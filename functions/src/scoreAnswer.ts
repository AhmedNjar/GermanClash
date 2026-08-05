import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { getFirestore } from "firebase-admin/firestore";
import { initializeApp } from "firebase-admin/app";

initializeApp();
const db = getFirestore();

const POINTS_CORRECT = 100;

interface AnswerDoc {
  playerId: string;
  answerId: string;
}

interface PlayerDoc {
  id: string;
  score: number;
}

/**
 * Fires whenever a player writes to game_sessions/{roomId}/answers/{playerId}.
 * Scores the answer against the session's current question and writes the
 * result back onto the session doc under lastResults.{playerId} - the
 * client's FirestoreDataSource listens for that field to reconcile the
 * optimistic RoundResult it returned immediately after submitAnswer().
 */
export const scoreAnswer = onDocumentCreated(
  "game_sessions/{roomId}/answers/{playerId}",
  async (event) => {
    const { roomId, playerId } = event.params as { roomId: string; playerId: string };
    const answerSnapshot = event.data;
    if (!answerSnapshot) return;

    const answerData = answerSnapshot.data() as AnswerDoc;
    const sessionRef = db.collection("game_sessions").doc(roomId);

    await db.runTransaction(async (transaction) => {
      const sessionDoc = await transaction.get(sessionRef);
      const session = sessionDoc.data();
      if (!session?.currentQuestion) return;

      const correctAnswerId: string | undefined = session.currentQuestion.correctAnswerId;
      const wasCorrect = correctAnswerId != null && correctAnswerId === answerData.answerId;
      const pointsAwarded = wasCorrect ? POINTS_CORRECT : 0;

      const players: PlayerDoc[] = session.players ?? [];
      const updatedPlayers = players.map((player) =>
        player.id === playerId ? { ...player, score: player.score + pointsAwarded } : player
      );

      transaction.update(sessionRef, {
        players: updatedPlayers,
        [`lastResults.${playerId}`]: {
          questionId: session.currentQuestion.id,
          wasCorrect,
          correctAnswerId: correctAnswerId ?? "",
          pointsAwarded
        }
      });
    });
  }
);
