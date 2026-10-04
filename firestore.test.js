const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user: cannot read transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Authenticated user: cannot read another user's transactions", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Authenticated user: can create valid transaction under their own account", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txDoc = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1");
  await assertSucceeds(
    txDoc.set({
      id: "tx_1",
      userId: ALICE_UID,
      type: "income",
      amountMinorUnits: 500000,
      currencyCode: "PKR",
      categoryId: "cat_salary",
      categoryNameSnapshot: "Salary",
      transactionDate: "2026-10-01",
      transactionYearMonth: "2026-10",
      description: "Monthly salary",
      paymentMethod: "Bank Transfer",
      isRecurring: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create transaction for another user", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const txDoc = bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_2");
  await assertFails(
    txDoc.set({
      id: "tx_2",
      userId: ALICE_UID,
      type: "expense",
      amountMinorUnits: 1500,
      currencyCode: "PKR",
      categoryId: "cat_food",
      categoryNameSnapshot: "Food",
      transactionDate: "2026-10-02",
      transactionYearMonth: "2026-10",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read categories and budgets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const catDoc = aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_1");
  await assertSucceeds(
    catDoc.set({
      id: "cat_1",
      userId: ALICE_UID,
      name: "Groceries",
      icon: "shopping_cart",
      colorHex: "#4CAF50",
      type: "expense",
      isDefault: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  const budgetDoc = aliceDb.collection("users").doc(ALICE_UID).collection("budgets").doc("budget_2026_10");
  await assertSucceeds(
    budgetDoc.set({
      id: "budget_2026_10",
      userId: ALICE_UID,
      month: "2026-10",
      overallLimitMinorUnits: 1500000,
      savingsTargetMinorUnits: 300000,
      categoryLimits: { "cat_1": 400000 },
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
