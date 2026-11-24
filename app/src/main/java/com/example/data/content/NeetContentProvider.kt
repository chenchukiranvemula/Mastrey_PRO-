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
 * Strictly isolated NEET UG content engine.
 * Contains Physics, Chemistry, Botany, and Zoology ONLY (no Mathematics subject).
 */
object NeetContentProvider {

    private val examId = ExamSystem.NEET_UG.id

    fun subjects(): List<SubjectItem> = listOf(
        SubjectItem(
            id = "neet_phy",
            examId = examId,
            name = "Physics",
            subtitle = "NCERT Mechanics, Ray Optics, Electricity & Modern Physics",
            iconName = "bolt",
            weightageNote = "NEET UG • 180 / 720 Marks"
        ),
        SubjectItem(
            id = "neet_chem",
            examId = examId,
            name = "Chemistry",
            subtitle = "NCERT Physical, Organic & Inorganic Concepts",
            iconName = "science",
            weightageNote = "NEET UG • 180 / 720 Marks"
        ),
        SubjectItem(
            id = "neet_botany",
            examId = examId,
            name = "Botany",
            subtitle = "Cell Biology, Plant Physiology, Genetics & Ecology",
            iconName = "eco",
            weightageNote = "NEET UG • 180 / 720 Marks"
        ),
        SubjectItem(
            id = "neet_zoology",
            examId = examId,
            name = "Zoology",
            subtitle = "Human Physiology, Reproduction, Immunity & Biotechnology",
            iconName = "biotech",
            weightageNote = "NEET UG • 180 / 720 Marks"
        )
    )

    fun chapters(): List<ChapterItem> = listOf(
        // BOTANY
        ChapterItem(
            id = "neet_bot_cell",
            subjectId = "neet_botany",
            examId = examId,
            order = 1,
            title = "Cell: The Unit of Life (NCERT)",
            introduction = "Explores prokaryotic vs eukaryotic cell architecture, fluid mosaic plasma membrane, endomembrane system, mitochondria, and plastids.",
            learningObjectives = listOf(
                "Distinguish prokaryotic 70S ribosomes and mesosomes from eukaryotic organelles.",
                "Identify members of the endomembrane system (ER, Golgi, Lysosomes, Vacuoles).",
                "Understand semi-autonomous nature of mitochondria and chloroplasts."
            ),
            importanceTag = "NCERT High Yield",
            estimatedMinutes = 40,
            topics = listOf(
                TopicItem(
                    id = "neet_bot_cell_t1",
                    chapterId = "neet_bot_cell",
                    title = "Endomembrane System & Semi-Autonomous Organelles",
                    subtopics = listOf("Fluid Mosaic Model", "ER & Golgi packaging", "Mitochondria vs Chloroplast"),
                    beginnerTheory = "Every living organism is built from cells. Eukaryotic cells have membrane-bound compartments called organelles. Four organelles—ER, Golgi apparatus, lysosomes, and vacuoles—work together like a postal pipeline called the endomembrane system.",
                    standardTheory = "Singer & Nicolson (1972) proposed the Fluid Mosaic Model: quasi-fluid lipid bilayer allowing lateral movement of proteins. Endomembrane system comprises Endoplasmic Reticulum (RER: protein synthesis; SER: lipid & steroid synthesis), Golgi apparatus (cis-forming face, trans-maturing face; glycosylation of glycoproteins/glycolipids), Lysosomes (acidic hydrolases, pH ~5), and Vacuoles (bounded by tonoplast). Mitochondria and chloroplasts are NOT part of the endomembrane system; both possess double membranes, circular DNA, and 70S ribosomes.",
                    advancedTheory = "In plant cells, tonoplast actively transports ions into the vacuole against a concentration gradient, making vacuolar ion concentration higher than cytoplasm. Mitochondrial inner membrane cristae house F₀-F₁ ATP synthase complexes, while chloroplast thylakoids organize light-harvesting photosystems and stroma contains Rubisco.",
                    keyDefinitions = listOf(
                        "Tonoplast: Single semi-permeable membrane surrounding the plant cell vacuole.",
                        "Cisternae: Stacked disc-shaped sacs of the Golgi apparatus (0.5–1.0 μm diameter).",
                        "Polysome: Several ribosomes attached to a single mRNA translating proteins simultaneously."
                    ),
                    derivationNote = null,
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "Which organelles are excluded from the endomembrane system because their functions are not coordinated with ER/Golgi?",
                            steps = listOf(
                                "Endomembrane members: ER, Golgi apparatus, Lysosomes, Vacuoles.",
                                "Non-members: Mitochondria, Chloroplasts, and Peroxisomes."
                            ),
                            finalAnswer = "Mitochondria, Chloroplasts, and Peroxisomes"
                        )
                    ),
                    shortcuts = listOf(
                        "Mnemonic for Endomembrane System: GERL-V (Golgi, ER, Lysosomes, Vacuoles).",
                        "Non-membrane-bound organelles: Ribosomes and Centrosome (centrioles)."
                    ),
                    commonMistakes = listOf(
                        "Including Peroxisomes or Mitochondria inside the endomembrane system.",
                        "Confusing 70S ribosomes (prokaryotes, mitochondria, chloroplasts) with 80S eukaryotic cytoplasmic ribosomes."
                    ),
                    examTips = listOf(
                        "NEET frequently asks NCERT exact scientist years: Singer & Nicolson (1972), Camillo Golgi (1898), George Palade (1953 ribosomes)."
                    ),
                    revisionPoints = listOf(
                        "Endomembrane = ER + Golgi + Lysosome + Vacuole.",
                        "Mitochondria & Chloroplasts have double membrane, circular dsDNA, 70S ribosomes."
                    )
                )
            )
        ),
        ChapterItem(
            id = "neet_bot_photo",
            subjectId = "neet_botany",
            examId = examId,
            order = 2,
            title = "Photosynthesis in Higher Plants",
            introduction = "Covers light reactions (PS II, PS I, Z-scheme), Calvin (C3) cycle, C4 Kranz anatomy, and photorespiration.",
            learningObjectives = listOf(
                "Compare cyclic and non-cyclic photophosphorylation.",
                "Calculate ATP and NADPH cost per glucose in C3 (18 ATP, 12 NADPH) vs C4 (30 ATP, 12 NADPH)."
            ),
            importanceTag = "High Yield",
            estimatedMinutes = 50,
            topics = emptyList(),
            verificationStatus = VerificationStatus.AWAITING_VERIFIED
        ),

        // ZOOLOGY
        ChapterItem(
            id = "neet_zoo_breathing",
            subjectId = "neet_zoology",
            examId = examId,
            order = 1,
            title = "Breathing & Exchange of Gases",
            introduction = "Details pulmonary volumes, partial pressures, oxygen-haemoglobin dissociation curve, and neural regulation of respiration.",
            learningObjectives = listOf(
                "Memorize NCERT respiratory volumes (TV, IRV, ERV, RV) and capacities (VC, TLC, FRC).",
                "Interpret Bohr's effect and shifts in the O₂-Hb sigmoid dissociation curve.",
                "Explain CO₂ transport as bicarbonate (70%), carbamino-Hb (20–25%), and dissolved plasma (7%)."
            ),
            importanceTag = "NCERT High Yield",
            estimatedMinutes = 40,
            topics = listOf(
                TopicItem(
                    id = "neet_zoo_breath_t1",
                    chapterId = "neet_zoo_breathing",
                    title = "Respiratory Volumes & Oxygen-Haemoglobin Curve",
                    subtopics = listOf("Spirometry volumes", "Alveolar gas exchange", "Right vs Left shift of O₂-Hb curve"),
                    beginnerTheory = "When we breathe normally, we move about 500 mL of air per breath (Tidal Volume). Oxygen binds reversibly to iron-containing haemoglobin in red blood cells to form oxyhaemoglobin and is released in active body tissues.",
                    standardTheory = "NCERT Standard Values: TV = 500 mL, IRV = 2500–3000 mL, ERV = 1000–1100 mL, RV = 1100–1200 mL. Key capacities: Inspiratory Capacity IC = TV + IRV; Functional Residual Capacity FRC = ERV + RV; Vital Capacity VC = ERV + TV + IRV; Total Lung Capacity TLC = VC + RV. Every 100 mL of oxygenated blood delivers ~5 mL O₂ to tissues under normal physiological conditions.",
                    advancedTheory = "O₂-Hb dissociation curve is sigmoid due to cooperative binding across 4 haem subunits. Shift to the RIGHT (favors O₂ dissociation in tissues) is caused by: Low pO₂, High pCO₂, High H⁺ (low pH), High Temperature, and High 2,3-BPG. Shift to the LEFT (favors oxyhaemoglobin formation in alveoli) occurs with High pO₂, Low pCO₂, Low H⁺ (higher pH), and Lower Temperature.",
                    keyDefinitions = listOf(
                        "Residual Volume (RV): Air remaining in lungs even after forcible expiration (~1100–1200 mL); cannot be measured directly by simple spirometer.",
                        "Vital Capacity (VC): Maximum volume of air a person can breathe in after a forced expiration (ERV + TV + IRV)."
                    ),
                    derivationNote = null,
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "If TV = 500 mL, IRV = 3000 mL, ERV = 1100 mL, and RV = 1200 mL, compute Functional Residual Capacity (FRC) and Vital Capacity (VC).",
                            steps = listOf(
                                "FRC = ERV + RV = 1100 + 1200 = 2300 mL.",
                                "VC = IRV + TV + ERV = 3000 + 500 + 1100 = 4600 mL."
                            ),
                            finalAnswer = "FRC = 2300 mL, VC = 4600 mL"
                        )
                    ),
                    shortcuts = listOf(
                        "Right shift mnemonic (CADET face RIGHT): CO₂↑, Acid(H⁺)↑, 2,3-DPG↑, Exercise, Temp↑.",
                        "CO₂ transport split: 70% HCO₃⁻ | 20–25% Carbamino-Hb | 7% dissolved plasma."
                    ),
                    commonMistakes = listOf(
                        "Confusing FRC (ERV + RV) with Expiratory Capacity EC (TV + ERV).",
                        "Thinking respiratory rhythm center is in Pons (rhythm center is in Medulla; Pneumotaxic center is in Pons)."
                    ),
                    examTips = listOf(
                        "Expect assertion-reason questions on alveoli vs tissue conditions shifting the O₂-Hb curve."
                    ),
                    revisionPoints = listOf(
                        "FRC = ERV + RV | VC = ERV + TV + IRV | TLC = VC + RV",
                        "100 mL blood delivers ~5 mL O₂ to tissues and ~4 mL CO₂ to alveoli."
                    )
                )
            )
        ),

        // PHYSICS (NEET)
        ChapterItem(
            id = "neet_phy_optics",
            subjectId = "neet_phy",
            examId = examId,
            order = 1,
            title = "Ray Optics & Optical Instruments",
            introduction = "Covers mirror formula, Snell's law, Total Internal Reflection, lens maker's formula, prisms, and microscopes.",
            learningObjectives = listOf(
                "Apply Cartesian sign conventions accurately to mirrors and thin lenses.",
                "Use Lens Maker's Formula and power of lens combinations."
            ),
            importanceTag = "High Yield",
            estimatedMinutes = 45,
            topics = listOf(
                TopicItem(
                    id = "neet_phy_opt_t1",
                    chapterId = "neet_phy_optics",
                    title = "Mirror, Lens & Lens Maker's Formula",
                    subtopics = listOf("Sign convention", "Magnification", "Power of lens in contact"),
                    beginnerTheory = "Mirrors reflect light while lenses refract (bend) light to focus or spread beams. Power P = 1/f (in meters) tells how strongly a lens bends rays, measured in Dioptres (D).",
                    standardTheory = "Mirror equation: 1/v + 1/u = 1/f with m = −v/u. Thin lens equation: 1/v − 1/u = 1/f with m = +v/u. Lens Maker's Formula: 1/f = (n₂/n₁ − 1)(1/R₁ − 1/R₂). Thin lenses in contact: P_eq = P₁ + P₂ and 1/f_eq = 1/f₁ + 1/f₂.",
                    advancedTheory = "When a lens of refractive index n_lens is immersed in a medium of index n_med > n_lens, the sign of (n_lens/n_med − 1) flips: a converging convex lens behaves as a diverging lens! For Total Internal Reflection, sin(i_c) = n_rarer / n_denser.",
                    keyDefinitions = listOf(
                        "Dioptre (D): Power of a lens with focal length 1 meter (P = 1/f_m = 100/f_cm).",
                        "Critical Angle (i_c): Angle of incidence in denser medium giving 90° refraction."
                    ),
                    derivationNote = "Refracting sequentially across spherical surfaces R₁ and R₂ cancels intermediate image distance to yield 1/f = (n−1)(1/R₁ − 1/R₂).",
                    solvedExamples = listOf(
                        SolvedExample(
                            problem = "A convex lens of focal length +20 cm is placed in contact with a concave lens of focal length −50 cm. Find combined power.",
                            steps = listOf(
                                "P₁ = 100 / 20 = +5 D.",
                                "P₂ = 100 / (−50) = −2 D.",
                                "P_eq = P₁ + P₂ = +5 − 2 = +3 D (converging)."
                            ),
                            finalAnswer = "+3 D (f_eq = +33.3 cm)"
                        )
                    ),
                    shortcuts = listOf(
                        "Cutting a symmetric biconvex lens perpendicular to principal axis doubles focal length (f → 2f).",
                        "Cutting along the principal axis keeps focal length unchanged (f stays f) but halves intensity."
                    ),
                    commonMistakes = listOf(
                        "Swapping the sign between Mirror (1/v + 1/u = 1/f) and Lens (1/v − 1/u = 1/f).",
                        "Using f in centimeters inside P = 1/f without multiplying by 100."
                    ),
                    examTips = listOf(
                        "NEET loves lens-cutting and medium-immersion numericals—practice sign checks."
                    ),
                    revisionPoints = listOf(
                        "Lens: 1/v − 1/u = 1/f | Mirror: 1/v + 1/u = 1/f",
                        "P = 100 / f(cm) Dioptres | sin(i_c) = 1/n"
                    )
                )
            )
        ),

        // CHEMISTRY (NEET)
        ChapterItem(
            id = "neet_chem_mole",
            subjectId = "neet_chem",
            examId = examId,
            order = 1,
            title = "Some Basic Concepts & Biomolecules",
            introduction = "Foundation of stoichiometry, molarity/molality, limiting reagent, plus NCERT carbohydrates, amino acids, and nucleic acids.",
            learningObjectives = listOf(
                "Distinguish temperature-dependent Molarity (M) from temperature-independent Molality (m).",
                "Identify limiting reagents and empirical formulas accurately."
            ),
            importanceTag = "Foundation",
            estimatedMinutes = 40,
            topics = emptyList(),
            verificationStatus = VerificationStatus.AWAITING_VERIFIED
        )
    )

    fun formulas(): List<FormulaItem> = listOf(
        FormulaItem(
            id = "neet_f_lens",
            examId = examId,
            subjectId = "neet_phy",
            subjectName = "Physics",
            chapterId = "neet_phy_optics",
            chapterName = "Ray Optics & Optical Instruments",
            topicName = "Lens Maker's Formula",
            category = "Optics",
            title = "Lens Maker's Formula",
            expression = "1/f = (n₂/n₁ − 1) · (1/R₁ − 1/R₂)",
            variables = mapOf("f" to "Focal length", "n₂" to "Refractive index of lens", "n₁" to "Refractive index of medium", "R₁, R₂" to "Radii of curvature"),
            units = "m⁻¹ (gives f in meters)",
            explanation = "Determines focal length of a thin spherical lens from material refractive index and surface curvatures.",
            conditionsOfUse = "Thin lens with paraxial rays.",
            example = "Equiconvex glass lens (n=1.5, R₁=+20 cm, R₂=−20 cm) in air gives f = +20 cm.",
            keywords = listOf("lens maker", "optics", "focal length", "refractive index")
        ),
        FormulaItem(
            id = "neet_f_vc",
            examId = examId,
            subjectId = "neet_zoology",
            subjectName = "Zoology",
            chapterId = "neet_zoo_breathing",
            chapterName = "Breathing & Exchange of Gases",
            topicName = "Pulmonary Capacities",
            category = "Human Physiology",
            title = "Lung Capacities (NCERT)",
            expression = "VC = IRV + TV + ERV   |   FRC = ERV + RV   |   TLC = VC + RV",
            variables = mapOf("TV" to "500 mL", "IRV" to "2500–3000 mL", "ERV" to "1000–1100 mL", "RV" to "1100–1200 mL"),
            units = "Milliliters (mL)",
            explanation = "Standard NCERT human respiratory capacities used in pulmonary assessment.",
            conditionsOfUse = "Healthy adult resting reference values.",
            example = "Typical Vital Capacity ≈ 4000–4600 mL.",
            keywords = listOf("vital capacity", "frc", "tidal volume", "breathing", "lungs", "zoology")
        ),
        FormulaItem(
            id = "neet_f_molarity",
            examId = examId,
            subjectId = "neet_chem",
            subjectName = "Chemistry",
            chapterId = "neet_chem_mole",
            chapterName = "Some Basic Concepts & Biomolecules",
            topicName = "Concentration Terms",
            category = "Physical Chemistry",
            title = "Molarity & Dilution Equation",
            expression = "M = n_solute / V_solution(L)   |   M₁ V₁ = M₂ V₂",
            variables = mapOf("M" to "Molarity", "n_solute" to "Moles of solute", "V_solution" to "Volume of solution in Liters"),
            units = "mol/L (M)",
            explanation = "Measures molar concentration; depends on temperature because volume expands with heat.",
            conditionsOfUse = "Dilution law M₁V₁ = M₂V₂ holds when only solvent is added without chemical reaction.",
            example = "0.5 mol NaCl in 250 mL solution gives M = 0.5 / 0.25 = 2 M.",
            keywords = listOf("molarity", "molality", "dilution", "concentration", "mole")
        ),
        FormulaItem(
            id = "neet_f_ke",
            examId = examId,
            subjectId = "neet_phy",
            subjectName = "Physics",
            chapterId = "neet_phy_optics",
            chapterName = "Ray Optics & Optical Instruments",
            topicName = "Kinetic Energy & De Broglie",
            category = "Modern Physics",
            title = "Kinetic Energy & De Broglie Wavelength",
            expression = "K = ½ m v²   |   λ = h / √(2 m K)",
            variables = mapOf("K" to "Kinetic energy", "λ" to "De Broglie wavelength", "h" to "Planck's constant 6.626×10⁻³⁴ J·s"),
            units = "K in Joules (J), λ in meters (m)",
            explanation = "Links classical kinetic energy to wave nature of matter particles.",
            conditionsOfUse = "Non-relativistic particles; for electrons accelerated through V volts, λ ≈ 12.27 / √V Å.",
            example = "An electron accelerated through 100 V has λ ≈ 1.227 Å.",
            keywords = listOf("kinetic energy", "de broglie", "wavelength", "modern physics")
        )
    )

    fun questions(): List<QuestionItem> = listOf(
        QuestionItem(
            id = "neet_q1",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("which organelle is not part of endomembrane system"),
            examId = examId,
            subjectId = "neet_botany",
            subjectName = "Botany",
            chapterId = "neet_bot_cell",
            chapterName = "Cell: The Unit of Life (NCERT)",
            topicId = "neet_bot_cell_t1",
            topicName = "Endomembrane System",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "Which of the following cell organelles is NOT considered part of the endomembrane system according to NCERT?",
            options = listOf("Endoplasmic Reticulum", "Golgi Complex", "Peroxisomes", "Vacuoles"),
            correctAnswer = "Peroxisomes",
            solutionSteps = listOf(
                "The endomembrane system consists of ER, Golgi complex, Lysosomes, and Vacuoles because their functions are coordinated.",
                "Mitochondria, chloroplasts, and peroxisomes are excluded."
            ),
            explanation = "Direct NCERT Class 11 Chapter 8 concept—frequently tested in NEET UG.",
            relatedFormula = "Endomembrane = ER + Golgi + Lysosome + Vacuole"
        ),
        QuestionItem(
            id = "neet_q2",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("assertion reason oxygen haemoglobin dissociation curve right shift"),
            examId = examId,
            subjectId = "neet_zoology",
            subjectName = "Zoology",
            chapterId = "neet_zoo_breathing",
            chapterName = "Breathing & Exchange of Gases",
            topicId = "neet_zoo_breath_t1",
            topicName = "O₂-Hb Dissociation Curve",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.ASSERTION_REASON,
            questionText = "Assertion (A): In active body tissues, the oxygen-haemoglobin dissociation curve shifts to the right.\nReason (R): High pCO₂, high H⁺ concentration, and higher temperature favor dissociation of oxygen from oxyhaemoglobin.",
            options = listOf(
                "Both A and R are true and R is the correct explanation of A",
                "Both A and R are true but R is NOT the correct explanation of A",
                "A is true but R is false",
                "A is false but R is true"
            ),
            correctAnswer = "Both A and R are true and R is the correct explanation of A",
            solutionSteps = listOf(
                "Active tissues produce CO₂, H⁺ (lower pH), and heat.",
                "All three factors decrease Hb affinity for O₂ (Bohr effect), shifting the curve to the right and releasing O₂."
            ),
            explanation = "Right shift = oxygen unloading in tissues; Left shift = oxygen loading in alveoli.",
            relatedFormula = "Right shift: pO₂↓, pCO₂↑, H⁺↑, Temp↑"
        ),
        QuestionItem(
            id = "neet_q3",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("functional residual capacity equals ERV plus RV"),
            examId = examId,
            subjectId = "neet_zoology",
            subjectName = "Zoology",
            chapterId = "neet_zoo_breathing",
            chapterName = "Breathing & Exchange of Gases",
            topicId = "neet_zoo_breath_t1",
            topicName = "Respiratory Volumes",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "The volume of air that remains in the lungs after a normal expiration is called Functional Residual Capacity (FRC), which equals:",
            options = listOf("TV + ERV", "ERV + RV", "IRV + TV + ERV", "TV + IRV"),
            correctAnswer = "ERV + RV",
            solutionSteps = listOf(
                "After a normal expiration, both Expiratory Reserve Volume (ERV) and Residual Volume (RV) remain in the lungs.",
                "Therefore FRC = ERV + RV."
            ),
            explanation = "Note: TV + ERV is Expiratory Capacity (EC), whereas ERV + RV is FRC.",
            relatedFormula = "FRC = ERV + RV"
        ),
        QuestionItem(
            id = "neet_q4",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("convex lens +20 cm contact concave lens -50 cm power"),
            examId = examId,
            subjectId = "neet_phy",
            subjectName = "Physics",
            chapterId = "neet_phy_optics",
            chapterName = "Ray Optics & Optical Instruments",
            topicId = "neet_phy_opt_t1",
            topicName = "Power of Lens Combination",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "A thin convex lens of focal length 25 cm is placed in contact with a thin concave lens of focal length 50 cm. The power of the combination is:",
            options = listOf("+2 D", "−2 D", "+6 D", "+4 D"),
            correctAnswer = "+2 D",
            solutionSteps = listOf(
                "P₁ = 100 / (+25) = +4 D.",
                "P₂ = 100 / (−50) = −2 D.",
                "P_net = P₁ + P₂ = +4 − 2 = +2 D."
            ),
            explanation = "Convex lens has positive power; concave lens has negative power.",
            relatedFormula = "P = 100 / f(cm) ; P_eq = P₁ + P₂"
        ),
        QuestionItem(
            id = "neet_q5",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("concentration term independent of temperature molality"),
            examId = examId,
            subjectId = "neet_chem",
            subjectName = "Chemistry",
            chapterId = "neet_chem_mole",
            chapterName = "Some Basic Concepts & Biomolecules",
            topicId = "neet_chem_mole_t1",
            topicName = "Concentration Units",
            difficulty = QuestionDifficulty.EASY,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "Which of the following concentration terms is independent of temperature?",
            options = listOf("Molarity", "Normality", "Molality", "Volume percentage (% v/v)"),
            correctAnswer = "Molality",
            solutionSteps = listOf(
                "Molality = moles of solute / mass of solvent (kg).",
                "Since mass does not vary with temperature (unlike volume), molality is temperature-independent."
            ),
            explanation = "Molarity, Normality, and % v/v all depend on solution volume, which changes with temperature.",
            relatedFormula = "m = n_solute / W_solvent(kg)"
        ),
        QuestionItem(
            id = "neet_q6",
            fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("plant vacuole membrane tonoplast active transport"),
            examId = examId,
            subjectId = "neet_botany",
            subjectName = "Botany",
            chapterId = "neet_bot_cell",
            chapterName = "Cell: The Unit of Life (NCERT)",
            topicId = "neet_bot_cell_t1",
            topicName = "Vacuoles & Tonoplast",
            difficulty = QuestionDifficulty.MEDIUM,
            questionType = QuestionType.SINGLE_CHOICE,
            questionText = "In plant cells, why is the concentration of ions significantly higher inside the vacuole than in the cytoplasm?",
            options = listOf(
                "Passive osmosis across the cell wall",
                "Facilitated diffusion through plasmodesmata",
                "Active transport of ions across the tonoplast against the concentration gradient",
                "Pinocytosis by lysosomes"
            ),
            correctAnswer = "Active transport of ions across the tonoplast against the concentration gradient",
            solutionSteps = listOf(
                "The vacuole is bounded by a single membrane called the tonoplast.",
                "In plants, the tonoplast actively pumps ions against their concentration gradient into the vacuole."
            ),
            explanation = "Exact NCERT statement frequently tested in NEET Botany.",
            relatedFormula = "Tonoplast → Active transport (low → high concentration)"
        )
    )

    fun flashcards(): List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "neet_fc1",
            examId = examId,
            subjectId = "neet_botany",
            subjectName = "Botany",
            chapterId = "neet_bot_cell",
            chapterName = "Cell: The Unit of Life (NCERT)",
            category = FlashcardCategory.DEFINITION,
            difficulty = QuestionDifficulty.EASY,
            front = "Which two organelles are semi-autonomous with circular DNA and 70S ribosomes?",
            back = "Mitochondria and Chloroplasts (both double-membrane-bound and excluded from the endomembrane system).",
            hint = "Powerhouse + photosynthesis organelle."
        ),
        FlashcardItem(
            id = "neet_fc2",
            examId = examId,
            subjectId = "neet_zoology",
            subjectName = "Zoology",
            chapterId = "neet_zoo_breathing",
            chapterName = "Breathing & Exchange of Gases",
            category = FlashcardCategory.CONCEPT,
            difficulty = QuestionDifficulty.MEDIUM,
            front = "How is CO₂ transported in human blood (NCERT percentages)?",
            back = "70% as bicarbonate ions (HCO₃⁻), 20–25% as carbamino-haemoglobin, and ~7% dissolved in plasma.",
            hint = "Carbonic anhydrase inside RBCs accelerates the largest fraction."
        ),
        FlashcardItem(
            id = "neet_fc3",
            examId = examId,
            subjectId = "neet_phy",
            subjectName = "Physics",
            chapterId = "neet_phy_optics",
            chapterName = "Ray Optics & Optical Instruments",
            category = FlashcardCategory.FORMULA,
            difficulty = QuestionDifficulty.MEDIUM,
            front = "What happens to a glass convex lens (n = 1.5) when immersed in carbon disulphide (n = 1.65)?",
            back = "It behaves as a DIVERGING (concave) lens because n_lens / n_medium < 1 makes focal length negative.",
            hint = "Check the sign of (n₂/n₁ − 1) in the Lens Maker's Formula."
        )
    )
}
