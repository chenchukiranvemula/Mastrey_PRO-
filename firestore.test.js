const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedExam: "JEE_MAIN",
      targetYear: 2027,
      prepLevel: "INTERMEDIATE",
      dailyStudyMinutes: 120,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tests").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookmarks").get());
});

test("UserProfile: owner create/update succeeds; shadow fields, future timestamps, and cross-user access fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav Sharma",
      avatarIndex: 2,
      selectedExam: "JEE_MAIN",
      targetYear: 2027,
      prepLevel: "INTERMEDIATE",
      dailyStudyMinutes: 180,
      preferredLanguage: "English",
      xp: 120,
      streakDays: 3,
      lastStudyDate: "2026-11-23",
      totalStudyMinutes: 45,
      lastChapterId: "jee_phy_kinematics",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 220,
      selectedExam: "JEE_ADVANCED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject shadow field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject tampered future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 300,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject cross-user read and write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Attempts & Tests: owner create/list succeeds; updates rejected; cross-user blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").doc("att_1").set({
      userId: ALICE_UID,
      examId: "NEET_UG",
      subjectId: "neet_bio_botany",
      chapterId: "neet_bot_cell",
      topicId: "neet_bot_cell_t1",
      questionId: "q_neet_01",
      questionFingerprint: "fp_neet_01",
      selectedAnswer: "B",
      correct: true,
      skipped: false,
      difficulty: "MEDIUM",
      timeSpentSec: 34,
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").get()
  );

  // Attempts are immutable once logged
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").doc("att_1").update({
      correct: false,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tests").doc("test_1").set({
      userId: ALICE_UID,
      examId: "JEE_MAIN",
      title: "Physics Kinematics Mock",
      mode: "EXAM",
      totalQuestions: 10,
      correctCount: 8,
      incorrectCount: 1,
      unattemptedCount: 1,
      score: 31,
      maxScore: 40,
      accuracyPercent: 88.9,
      durationMinutes: 20,
      timeTakenSec: 940,
      subjectSummary: ["Physics:8/10"],
      weakTopics: ["Relative Velocity"],
      createdAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("tests").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("tests").get());
});

test("Bookmarks, Notes, Revisions, StudyTasks, AI Conversations & Reports enforce ownership and schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Bookmark
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("bm_1").set({
      userId: ALICE_UID,
      examId: "JEE_MAIN",
      subjectId: "jee_phy",
      chapterId: "jee_phy_wpe",
      itemType: "FORMULA",
      targetId: "f_ke",
      title: "Kinetic Energy",
      snippet: "K = 1/2 m v^2",
      collectionName: "High Yield",
      createdAt: now,
    })
  );

  // Note
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      examId: "CA_FOUNDATION",
      subjectId: "ca_acc",
      chapterId: "ca_acc_basics",
      title: "Golden Rules",
      content: "Debit what comes in, credit what goes out.",
      highlighted: true,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      highlighted: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Revision
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("revisions").doc("topic_ke").set({
      userId: ALICE_UID,
      examId: "JEE_MAIN",
      subjectId: "jee_phy",
      chapterId: "jee_phy_wpe",
      topicId: "topic_ke",
      topicTitle: "Work-Energy Theorem",
      confidence: "NEED_REVISION",
      reviewCount: 1,
      nextReviewEpochMs: Date.now() + 86400000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Study task
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_tasks").doc("task_1").set({
      userId: ALICE_UID,
      examId: "NEET_UG",
      title: "Revise Photosynthesis C3 vs C4",
      subjectName: "Botany",
      chapterId: "neet_bot_photo",
      scheduledDate: "2026-11-24",
      durationMinutes: 45,
      taskType: "REVISION",
      completed: false,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_tasks").doc("task_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // AI Conversation + Message
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_conversations").doc("conv_1").set({
      userId: ALICE_UID,
      examProfile: "JEE",
      title: "Rotational Inertia Help",
      responseStyle: "Step-by-step",
      messageCount: 1,
      lastMessagePreview: "Explain parallel axis theorem",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ai_conversations")
      .doc("conv_1")
      .collection("messages")
      .doc("msg_1")
      .set({
        userId: ALICE_UID,
        role: "USER",
        content: "Explain parallel axis theorem",
        bookmarked: false,
        flagged: false,
        createdAt: now,
      })
  );

  // Report
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("reports").doc("rep_1").set({
      userId: ALICE_UID,
      examId: "JEE_MAIN",
      targetType: "QUESTION",
      targetId: "q_1",
      reason: "Clarify sign convention in solution",
      createdAt: now,
    })
  );

  // Cross-user denial across subcollections
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("ai_conversations").get());
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ai_conversations")
      .doc("conv_1")
      .collection("messages")
      .get()
  );
});
