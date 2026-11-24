package com.example.data.content

import com.example.data.ai.FirebaseMastreyAiService
import com.example.data.model.ChapterItem
import com.example.data.model.ExamSystem
import com.example.data.model.FlashcardCategory
import com.example.data.model.FlashcardItem
import com.example.data.model.FormulaItem
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.SolvedExample
import com.example.data.model.SubjectItem
import com.example.data.model.TopicItem
import com.example.data.model.VerificationStatus

/**
 * Strictly isolated CA Foundation content engine (ICAI New Scheme).
 */
object CaContentProvider {

    private val examId = ExamSystem.CA_FOUNDATION.id

    fun subjects(): List<SubjectItem> = listOf(
        SubjectItem(
            id = "ca_acc",
            examId = examId,
            name = "Accounting",
            subtitle = "Golden Rules, Journal, Ledger, BRS & Depreciation",
            iconName = "account_balance",
            weightageNote = "Paper 1 • 100 Marks"
        ),
        SubjectItem(
            id = "ca_law",
            examId = examId,
            name = "Business Laws",
            subtitle = "Indian Contract Act, Sale of Goods & Partnership Act",
            iconName = "gavel",
            weightageNote = "Paper 2 • 100 Marks"
        ),
        SubjectItem(
            id = "ca_qa",
            examId = examId,
            name = "Quantitative Aptitude",
            subtitle = "Business Mathematics, Logical Reasoning & Statistics",
            iconName = "calculate",
            weightageNote = "Paper 3 • 100 Marks (MCQ)"
        ),
        SubjectItem(
            id = "ca_eco",
            examId = examId,
            name = "Business Economics",
            subtitle = "Demand & Supply, Production, Markets & Macro Basics",
            iconName = "trending_up",
            weightageNote = "Paper 4 • 100 Marks (MCQ)"
        )
    )

    fun chapters(): List<ChapterItem> = listOf(
        // ACCOUNTING
        ChapterItem(
            id = "ca_acc_journal",
            subjectId = "ca_acc",
            examId = examId,
            order = 1,
            title = "Accounting Process: Journal & Ledger",
            introduction = "Builds double-entry mastery using Traditional Golden Rules, Modern Accounting Equation, journal entries, and trial balance.",
            learningObjectives = listOf(
                "Classify accounts into Personal, Real, and Nominal categories.",
                "Pass accurate journal entries for purchases, sales, discounts, and bad debts.",
                "Verify double-entry accuracy via Accounting Equation: Assets = Liabilities + Capital."
            ),
            importanceTag = "Core Foundation",
            estimatedMinutes = 45,
            topics = listOf(
                TopicItem(
                    id = "ca_acc_j_t1",
                    chapterId = "ca_acc_journal",
                    title = "Golden Rules & Double-Entry Journalizing",
                    subtopics = listOf("Traditional rules", "Modern equation approach", "Trade vs Cash discount"),
                    beginnerTheory = "Every business transaction has two equal sides: a Debit (Dr.) and a Credit (Cr.). If a business buys furniture for ₹10,000 cash, Furniture comes in (Debit) and Cash goes out (Credit), keeping the books balanced.",
                    standardTheory = "Traditional Golden Rules:\n• Personal A/c: Debit the receiver, Credit the giver.\n• Real A/c (Assets): Debit what comes in, Credit what goes out.\n• Nominal A/c (Expenses/Incomes): Debit all expenses & losses, Credit all incomes & gains.\n\nModern Approach: Assets & Expenses increase on Debit; Liabilities, Capital & Incomes increase on Credit. Trade Discount is deducted on the invoice and NOT recorded in books; Cash Discount IS recorded in books.",
                    advancedTheory = "Representative Personal Accounts (Outstanding Rent, Prepaid Insurance, Accrued Income) represent persons to whom money is owed or from whom it is due—never misclassify them as Nominal accounts! When goods are withdrawn by proprietor for personal use, debit Drawings A/c and credit Purchases A/c (at cost, not selling price).",
                    keyDefinitions = listOf(
                        "Journal: Book of original/prime entry recording transactions chronologically.",
                        "Ledger: Principal book of final entry grouping transactions account-wise.",
                        "Trade Discount: Price reduction on catalogue price for bulk buying; never journalized separately."
                    ),
                    derivationNote = "Accounting Equation: Assets = Capital + Liabilities + (Revenues − Expenses).",
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "Purchased goods of list price ₹20,000 from Mehta Traders at 10% trade discount and paid cash receiving 5% cash discount. Pass the journal entry.",
                            steps = listOf(
                                "Invoice price after 10% trade discount = ₹20,000 − ₹2,000 = ₹18,000 (Purchases A/c Dr.).",
                                "5% Cash discount on ₹18,000 = ₹900 (Discount Received A/c Cr.).",
                                "Net Cash paid = ₹18,000 − ₹900 = ₹17,100 (Cash A/c Cr.)."
                            ),
                            finalAnswer = "Purchases A/c Dr. ₹18,000 | To Cash A/c ₹17,100 | To Discount Received A/c ₹900"
                        )
                    ),
                    shortcuts = listOf(
                        "DEALER mnemonic: Debit increases Drawings, Expenses, Assets; Credit increases Liabilities, Equity, Revenue.",
                        "Any prefix like Outstanding / Prepaid / Accrued / Unearned turns a Nominal item into a Representative Personal Account!"
                    ),
                    commonMistakes = listOf(
                        "Recording Trade Discount in the journal (only Cash Discount is recorded).",
                        "Crediting Sales A/c instead of Purchases A/c when goods are distributed as free samples or drawings."
                    ),
                    examTips = listOf(
                        "Always write crisp narrations in brackets in descriptive Paper 1 answers."
                    ),
                    revisionPoints = listOf(
                        "Assets = Liabilities + Capital",
                        "Trade discount: NOT recorded | Cash discount: RECORDED"
                    )
                )
            )
        ),

        // BUSINESS LAWS
        ChapterItem(
            id = "ca_law_contract",
            subjectId = "ca_law",
            examId = examId,
            order = 1,
            title = "Indian Contract Act, 1872: Nature of Contracts",
            introduction = "Establishes essentials of a valid contract under Section 10, offer vs invitation to offer, acceptance, consideration, and enforceability.",
            learningObjectives = listOf(
                "Apply Section 2(h): Contract = Agreement enforceable by law.",
                "Distinguish valid Offer from Invitation to Offer (e.g., price tags, menus, tenders).",
                "Identify void, voidable, and illegal agreements."
            ),
            importanceTag = "High Weightage",
            estimatedMinutes = 50,
            topics = listOf(
                TopicItem(
                    id = "ca_law_c_t1",
                    chapterId = "ca_law_contract",
                    title = "Essentials of Valid Contract & Offer vs Invitation",
                    subtopics = listOf("Section 2(h) & Section 10", "Balfour v. Balfour", "Carlill v. Carbolic Smoke Ball"),
                    beginnerTheory = "Every contract starts as an agreement (Offer + Acceptance), but not every agreement is a contract. Social promises—like promising to take a friend to dinner—lack legal intent and cannot be sued upon in court.",
                    standardTheory = "Under Section 2(h), a Contract is an agreement enforceable by law. Section 10 essentials: Free consent, competent parties, lawful consideration, lawful object, and not expressly declared void. Additionally, intention to create legal relationship (Balfour v. Balfour), certainty of meaning, and possibility of performance are required. An Invitation to Offer (shop window display, auction notice, prospectus) merely invites others to make an offer.",
                    advancedTheory = "In Carlill v. Carbolic Smoke Ball Co., a general offer made to the public at large can be accepted by anyone performing the stated conditions without prior formal communication. Cross offers (identical offers crossing in post) do NOT form a contract because neither is an acceptance of the other.",
                    keyDefinitions = listOf(
                        "Section 2(e) Agreement: Every promise and every set of promises forming consideration for each other.",
                        "Section 2(i) Voidable Contract: Enforceable at the option of one or more parties, but not at the option of the other(s).",
                        "Quid Pro Quo: 'Something in return' — the legal essence of Consideration."
                    ),
                    derivationNote = null,
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "A shopkeeper displays a coat with a price tag of ₹2,500. Ravi offers ₹2,500 at the counter, but the shopkeeper refuses to sell. Can Ravi sue for breach of contract?",
                            steps = listOf(
                                "Legal Provision: Displaying goods with price tags is an 'Invitation to Offer', NOT an offer (Pharmaceutical Society v. Boots Cash Chemists).",
                                "Analysis: Ravi's offer to buy at ₹2,500 is the actual offer, which the shopkeeper is free to accept or reject.",
                                "Conclusion: No valid contract was formed; Ravi cannot sue."
                            ),
                            finalAnswer = "No — a price display is an invitation to offer, not a binding offer."
                        )
                    ),
                    shortcuts = listOf(
                        "All contracts are agreements, but all agreements are NOT contracts.",
                        "Case-law structure for Paper 2: 1. Provision of Law → 2. Facts & Analysis → 3. Conclusion."
                    ),
                    commonMistakes = listOf(
                        "Treating an auction advertisement or shop price tag as an offer.",
                        "Confusing 'void ab initio' (e.g., agreement with a minor) with 'voidable' (e.g., coercion/fraud)."
                    ),
                    examTips = listOf(
                        "Quote Section 2(h) and Section 10 cleanly in Business Law case answers."
                    ),
                    revisionPoints = listOf(
                        "Contract = Agreement + Legal Enforceability (Sec 2(h))",
                        "Shop displays, menus, catalogues, prospectuses = Invitation to Offer."
                    )
                )
            )
        ),

        // QUANTITATIVE APTITUDE
        ChapterItem(
            id = "ca_qa_tvm",
            subjectId = "ca_qa",
            examId = examId,
            order = 1,
            title = "Mathematics of Finance (Time Value of Money)",
            introduction = "High-scoring CA Foundation Quantitative Aptitude chapter covering Simple & Compound Interest, Effective Rate, and Annuities.",
            learningObjectives = listOf(
                "Compute compound amount A = P(1 + i)ⁿ and effective rate E = (1 + i/k)^k − 1.",
                "Calculate Future Value and Present Value of ordinary annuities and annuities due."
            ),
            importanceTag = "Highest Weightage",
            estimatedMinutes = 45,
            topics = listOf(
                TopicItem(
                    id = "ca_qa_tvm_t1",
                    chapterId = "ca_qa_tvm",
                    title = "Compound Interest, Effective Rate & Annuities",
                    subtopics = listOf("Nominal vs Effective rate", "Future Value of Annuity", "Present Value & Perpetuity"),
                    beginnerTheory = "A rupee today is worth more than a rupee tomorrow because today's rupee can earn interest. Compound interest earns interest on both the principal AND previously accumulated interest.",
                    standardTheory = "Compound Amount: A = P(1 + r/100k)^(kt). Effective Annual Rate: E = (1 + i)^k − 1 where i = nominal rate per compounding period. Future Value of Ordinary Annuity: FV = A · [((1+i)ⁿ − 1) / i]. Present Value of Perpetuity: PV = A / i.",
                    advancedTheory = "For Annuity Due (payments at the BEGINNING of each period), multiply the Ordinary Annuity value (FV or PV) by (1 + i). Sinking fund payments use the FV of annuity formula to accumulate a known future redemption amount.",
                    keyDefinitions = listOf(
                        "Annuity Regular (Ordinary): Equal periodic payments made at the END of each period.",
                        "Annuity Due: Equal periodic payments made at the BEGINNING of each period.",
                        "Perpetuity: An infinite stream of equal periodic cash flows (PV = A / i)."
                    ),
                    derivationNote = "Sum of geometric series A + A(1+i) + ... + A(1+i)^(n−1) = A·[((1+i)ⁿ − 1)/i].",
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "Find the effective annual interest rate corresponding to a nominal rate of 12% p.a. compounded semi-annually.",
                            steps = listOf(
                                "Semi-annual compounding means k = 2, rate per half-year i = 12% / 2 = 6% = 0.06.",
                                "E = (1 + 0.06)² − 1 = 1.1236 − 1 = 0.1236.",
                                "Effective rate = 12.36% p.a."
                            ),
                            finalAnswer = "12.36% p.a."
                        )
                    ),
                    shortcuts = listOf(
                        "Rule of 72 (quick check): Approximate doubling years ≈ 72 / r%.",
                        "Effective rate is ALWAYS greater than nominal rate when compounding frequency k > 1."
                    ),
                    commonMistakes = listOf(
                        "Forgetting to divide annual rate by k AND multiply years by k for quarterly/semi-annual compounding."
                    ),
                    examTips = listOf(
                        "TVM carries 12–15 marks in Paper 3; master calculator power shortcuts."
                    ),
                    revisionPoints = listOf(
                        "E = (1 + r/k)^k − 1 | Perpetuity PV = A / i",
                        "Annuity Due = Ordinary Annuity × (1 + i)"
                    )
                )
            )
        ),

        // BUSINESS ECONOMICS
        ChapterItem(
            id = "ca_eco_demand",
            subjectId = "ca_eco",
            examId = examId,
            order = 1,
            title = "Theory of Demand & Elasticity",
            introduction = "Explains determinants of demand, Law of Demand, Giffen/Veblen exceptions, and Price, Income & Cross Elasticity.",
            learningObjectives = listOf(
                "Compute Point, Arc, and Total Outlay Price Elasticity of Demand.",
                "Distinguish substitutes (positive cross elasticity) from complements (negative cross elasticity)."
            ),
            importanceTag = "High Yield",
            estimatedMinutes = 40,
            topics = emptyList(),
            verificationStatus = VerificationStatus.AWAITING_VERIFIED
        )
    )

    fun formulas(): List<FormulaItem> = listOf(
        FormulaItem(
            id = "ca_f_acc_eq",
            examId = examId,
            subjectId = "ca_acc",
            subjectName = "Accounting",
            chapterId = "ca_acc_journal",
            chapterName = "Accounting Process: Journal & Ledger",
            topicName = "Accounting Equation",
            category = "Accounting",
            title = "Fundamental Accounting Equation",
            expression = "Assets = Liabilities + Capital",
            variables = mapOf("Assets" to "Economic resources controlled by enterprise", "Liabilities" to "External obligations", "Capital" to "Owner's residual equity"),
            units = "Currency (₹)",
            explanation = "Expresses the dual-aspect concept: every debit has an equal and corresponding credit.",
            conditionsOfUse = "Holds at every instant for every double-entry balance sheet.",
            example = "If Assets = ₹5,00,000 and Liabilities = ₹1,80,000, Capital = ₹3,20,000.",
            keywords = listOf("accounting equation", "assets", "liabilities", "capital", "balance sheet")
        ),
        FormulaItem(
            id = "ca_f_eff_rate",
            examId = examId,
            subjectId = "ca_qa",
            subjectName = "Quantitative Aptitude",
            chapterId = "ca_qa_tvm",
            chapterName = "Mathematics of Finance (Time Value of Money)",
            topicName = "Effective Rate of Interest",
            category = "Financial Math",
            title = "Effective Annual Interest Rate",
            expression = "E = (1 + i / k)^k − 1",
            variables = mapOf("E" to "Effective annual rate", "i" to "Nominal annual rate (decimal)", "k" to "Compounding periods per year"),
            units = "Decimal / Percentage p.a.",
            explanation = "Actual annual return earned when interest is compounded k times per year.",
            conditionsOfUse = "Constant nominal rate across equal sub-periods.",
            example = "10% p.a. compounded half-yearly (k=2): E = (1.05)² − 1 = 10.25%.",
            keywords = listOf("effective rate", "compound interest", "tvm", "finance", "annuity")
        ),
        FormulaItem(
            id = "ca_f_annuity_fv",
            examId = examId,
            subjectId = "ca_qa",
            subjectName = "Quantitative Aptitude",
            chapterId = "ca_qa_tvm",
            chapterName = "Mathematics of Finance (Time Value of Money)",
            topicName = "Annuity Future Value",
            category = "Financial Math",
            title = "Future Value of Ordinary Annuity",
            expression = "FV = A · [ ((1 + i)ⁿ − 1) / i ]",
            variables = mapOf("A" to "Periodic payment", "i" to "Rate per period", "n" to "Number of periods"),
            units = "Currency (₹)",
            explanation = "Accumulated value of equal end-of-period deposits compounded at rate i.",
            conditionsOfUse = "Payments occur at the END of each period; multiply by (1+i) for Annuity Due.",
            example = "₹1,000/yr for 3 yrs at 10%: FV = 1000 × (1.331 − 1)/0.10 = ₹3,310.",
            keywords = listOf("annuity", "future value", "sinking fund", "tvm")
        ),
        FormulaItem(
            id = "ca_f_ped",
            examId = examId,
            subjectId = "ca_eco",
            subjectName = "Business Economics",
            chapterId = "ca_eco_demand",
            chapterName = "Theory of Demand & Elasticity",
            topicName = "Price Elasticity",
            category = "Microeconomics",
            title = "Price Elasticity of Demand",
            expression = "E_p = − (% ΔQ / % ΔP) = − (ΔQ / ΔP) · (P / Q)",
            variables = mapOf("E_p" to "Price elasticity coefficient", "ΔQ" to "Change in quantity demanded", "ΔP" to "Change in price"),
            units = "Dimensionless ratio",
            explanation = "Measures responsiveness of quantity demanded to a percentage change in price.",
            conditionsOfUse = "Ceteris paribus (other determinants held constant). E_p > 1 elastic, E_p < 1 inelastic.",
            example = "Price drops 10% and quantity rises 20% ⇒ E_p = 2 (relatively elastic).",
            keywords = listOf("elasticity", "demand", "economics", "price")
        )
    )

    fun questions(): List<QuestionItem> = listOf(
        QuestionItem(
            id = "ca_q1",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("outstanding salary account classification personal real nominal"),
            examId = examId,
            subjectId = "ca_acc",
            subjectName = "Accounting",
            chapterId = "ca_acc_journal",
            chapterName = "Accounting Process: Journal & Ledger",
            topicId = "ca_acc_j_t1",
            topicName = "Classification of Accounts",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "Under the traditional classification of accounts, 'Outstanding Salaries Account' is classified as a:",
            options = listOf("Nominal Account", "Real Account", "Representative Personal Account", "Valuation Account"),
            correctAnswer = "Representative Personal Account",
            solutionSteps = listOf(
                "Salary A/c by itself is a Nominal Account (expense).",
                "Adding the prefix 'Outstanding' represents employees to whom salary is payable, making it a Representative Personal Account."
            ),
            explanation = "All accrued, prepaid, outstanding, and unearned accounts are Representative Personal Accounts.",
            relatedFormula = "Prefix (Prepaid/Outstanding/Accrued) + Nominal = Representative Personal A/c"
        ),
        QuestionItem(
            id = "ca_q2",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("trade discount vs cash discount recorded in books"),
            examId = examId,
            subjectId = "ca_acc",
            subjectName = "Accounting",
            chapterId = "ca_acc_journal",
            chapterName = "Accounting Process: Journal & Ledger",
            topicId = "ca_acc_j_t1",
            topicName = "Journal Entries",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "Goods of list price ₹50,000 are purchased at 10% trade discount and 2% cash discount on immediate payment. What amount is debited to Purchases A/c?",
            options = listOf("₹50,000", "₹45,000", "₹44,100", "₹49,000"),
            correctAnswer = "₹45,000",
            solutionSteps = listOf(
                "Trade discount = 10% of ₹50,000 = ₹5,000 (deducted before recording).",
                "Purchases A/c is debited at net invoice value = ₹50,000 − ₹5,000 = ₹45,000.",
                "Cash discount (2% of ₹45,000 = ₹900) is credited separately to Discount Received A/c."
            ),
            explanation = "Purchases A/c is always recorded net of trade discount, before deducting cash discount.",
            relatedFormula = "Purchases Dr. = List Price − Trade Discount"
        ),
        QuestionItem(
            id = "ca_q3",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("display of goods with price tag invitation to offer case based"),
            examId = examId,
            subjectId = "ca_law",
            subjectName = "Business Laws",
            chapterId = "ca_law_contract",
            chapterName = "Indian Contract Act, 1872: Nature of Contracts",
            topicId = "ca_law_c_t1",
            topicName = "Offer vs Invitation to Offer",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.CASE_BASED,
            questionText = "Case: A retail store displays a watch with a price tag of ₹3,000. A customer picks it up and tenders ₹3,000 at the cash counter, but the cashier refuses citing a misprint. Under the Indian Contract Act, 1872, the display of goods with a price tag is:",
            options = listOf(
                "A binding general offer",
                "An invitation to offer",
                "A valid contract immediately upon display",
                "A cross offer"
            ),
            correctAnswer = "An invitation to offer",
            solutionSteps = listOf(
                "Under established contract law principles (Pharmaceutical Society v. Boots Cash Chemists), price-tagged displays are invitations to offer.",
                "The customer makes the offer at the checkout counter, which the seller may accept or decline."
            ),
            explanation = "Distinguishing an offer from an invitation to offer is a classic CA Foundation Paper 2 question.",
            relatedFormula = "Invitation to Offer → Customer's Offer → Seller's Acceptance"
        ),
        QuestionItem(
            id = "ca_q4",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("effective rate 12 percent compounded semi annually"),
            examId = examId,
            subjectId = "ca_qa",
            subjectName = "Quantitative Aptitude",
            chapterId = "ca_qa_tvm",
            chapterName = "Mathematics of Finance (Time Value of Money)",
            topicId = "ca_qa_tvm_t1",
            topicName = "Effective Interest Rate",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "What is the effective annual rate of interest corresponding to a nominal rate of 12% per annum compounded semi-annually?",
            options = listOf("12.00%", "12.25%", "12.36%", "12.68%"),
            correctAnswer = "12.36%",
            solutionSteps = listOf(
                "Compounding frequency k = 2, periodic rate i = 12% / 2 = 6% = 0.06.",
                "E = (1 + 0.06)² − 1 = 1.1236 − 1 = 0.1236 = 12.36%."
            ),
            explanation = "Semi-annual compounding earns interest on the first half-year's 6% interest: 6% + 6% + (6%×6%) = 12.36%.",
            relatedFormula = "E = (1 + i/k)^k − 1"
        ),
        QuestionItem(
            id = "ca_q5",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("present value of perpetuity 5000 at 10 percent"),
            examId = examId,
            subjectId = "ca_qa",
            subjectName = "Quantitative Aptitude",
            chapterId = "ca_qa_tvm",
            chapterName = "Mathematics of Finance (Time Value of Money)",
            topicId = "ca_qa_tvm_t1",
            topicName = "Perpetuity",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.NUMERICAL,
            questionText = "Find the present value (in ₹) of a perpetuity paying ₹5,000 at the end of every year forever, if the discount rate is 10% p.a.",
            options = listOf("₹25,000", "₹45,000", "₹50,000", "₹55,000"),
            correctAnswer = "₹50,000",
            solutionSteps = listOf(
                "Present Value of an ordinary perpetuity: PV = A / i.",
                "PV = 5,000 / 0.10 = ₹50,000."
            ),
            explanation = "₹50,000 invested at 10% generates ₹5,000 annually forever without touching principal.",
            relatedFormula = "PV_perpetuity = A / i"
        ),
        QuestionItem(
            id = "ca_q6",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("cross elasticity of demand complementary goods negative"),
            examId = examId,
            subjectId = "ca_eco",
            subjectName = "Business Economics",
            chapterId = "ca_eco_demand",
            chapterName = "Theory of Demand & Elasticity",
            topicId = "ca_eco_dem_t1",
            topicName = "Cross Elasticity",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "The cross elasticity of demand between two complementary goods (such as petrol and cars) is always:",
            options = listOf("Positive", "Negative", "Zero", "Infinite"),
            correctAnswer = "Negative",
            solutionSteps = listOf(
                "When the price of petrol rises, the quantity demanded for cars falls.",
                "Because price of Good Y and quantity of Good X move in opposite directions, cross elasticity E_c < 0."
            ),
            explanation = "Substitutes have positive cross elasticity; complements have negative cross elasticity; unrelated goods have zero.",
            relatedFormula = "E_c = (% ΔQ_x) / (% ΔP_y) < 0 for complements"
        )
    )

    fun flashcards(): List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "ca_fc1",
            examId = examId,
            subjectId = "ca_acc",
            subjectName = "Accounting",
            chapterId = "ca_acc_journal",
            chapterName = "Accounting Process: Journal & Ledger",
            category = FlashcardCategory.ACCOUNTING_ENTRY,
            difficulty = QuestionDifficulty.EASY,
            front = "Journal entry when proprietor withdraws goods costing ₹4,000 (selling price ₹5,000) for personal use?",
            back = "Drawings A/c Dr. ₹4,000\n   To Purchases A/c ₹4,000\n(Recorded at COST price, not selling price!)",
            hint = "Goods withdrawn reduce purchases at cost."
        ),
        FlashcardItem(
            id = "ca_fc2",
            examId = examId,
            subjectId = "ca_law",
            subjectName = "Business Laws",
            chapterId = "ca_law_contract",
            chapterName = "Indian Contract Act, 1872: Nature of Contracts",
            category = FlashcardCategory.LAW,
            difficulty = QuestionDifficulty.MEDIUM,
            front = "What principle was established in Balfour v. Balfour (1919)?",
            back = "Domestic or social agreements made without an intention to create legal relations are NOT enforceable contracts.",
            hint = "Think of domestic promises between spouses."
        ),
        FlashcardItem(
            id = "ca_fc3",
            examId = examId,
            subjectId = "ca_qa",
            subjectName = "Quantitative Aptitude",
            chapterId = "ca_qa_tvm",
            chapterName = "Mathematics of Finance (Time Value of Money)",
            category = FlashcardCategory.FORMULA,
            difficulty = QuestionDifficulty.EASY,
            front = "How do you convert Ordinary Annuity value into Annuity Due value?",
            back = "Multiply by (1 + i): FV_due = FV_ordinary × (1 + i) and PV_due = PV_ordinary × (1 + i).",
            hint = "Each payment earns/compounds for one extra period."
        )
    )
}
