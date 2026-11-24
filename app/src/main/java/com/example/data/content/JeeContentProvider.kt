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

object JeeContentProvider {

    fun subjects(exam: ExamSystem): List<SubjectItem> {
        val tag = if (exam == ExamSystem.JEE_ADVANCED) "JEE Advanced" else "JEE Main"
        return listOf(
            SubjectItem(
                id = "${exam.id}_phy",
                examId = exam.id,
                name = "Physics",
                subtitle = if (exam == ExamSystem.JEE_ADVANCED) "Mechanics, Electrodynamics & Multi-Concept Problems" else "Mechanics, Optics, Electricity & Modern Physics",
                iconName = "bolt",
                weightageNote = "$tag • 33.3% Weightage"
            ),
            SubjectItem(
                id = "${exam.id}_chem",
                examId = exam.id,
                name = "Chemistry",
                subtitle = if (exam == ExamSystem.JEE_ADVANCED) "Physical Thermodynamics, Reaction Mechanisms & Coordination" else "Physical, Organic & Inorganic Fundamentals",
                iconName = "science",
                weightageNote = "$tag • 33.3% Weightage"
            ),
            SubjectItem(
                id = "${exam.id}_math",
                examId = exam.id,
                name = "Mathematics",
                subtitle = if (exam == ExamSystem.JEE_ADVANCED) "Calculus, Matrices, Vectors & Proof-Style Problem Solving" else "Algebra, Calculus, Coordinate Geometry & Vectors",
                iconName = "functions",
                weightageNote = "$tag • 33.3% Weightage"
            )
        )
    }

    fun chapters(exam: ExamSystem): List<ChapterItem> {
        val phyId = "${exam.id}_phy"
        val chemId = "${exam.id}_chem"
        val mathId = "${exam.id}_math"
        val adv = exam == ExamSystem.JEE_ADVANCED

        return listOf(
            // PHYSICS
            ChapterItem(
                id = "${exam.id}_phy_kinematics",
                subjectId = phyId,
                examId = exam.id,
                order = 1,
                title = "Kinematics & Projectile Motion",
                introduction = "Describes the geometry of motion in 1D and 2D using displacement, velocity, acceleration, relative motion, and projectile trajectories.",
                learningObjectives = listOf(
                    "Apply equations of uniformly accelerated motion and calculus relations v = dx/dt, a = dv/dt.",
                    "Resolve oblique projectile trajectories into independent horizontal and vertical components.",
                    "Solve river-boat, rain-umbrella, and moving-observer relative velocity problems."
                ),
                importanceTag = "High Yield",
                estimatedMinutes = 45,
                topics = listOf(
                    TopicItem(
                        id = "${exam.id}_phy_kin_t1",
                        chapterId = "${exam.id}_phy_kinematics",
                        title = "Equations of Motion & Projectile Trajectory",
                        subtopics = listOf("Uniform acceleration", "Time of flight & Range", "Equation of trajectory"),
                        beginnerTheory = "When an object moves with constant acceleration a, its velocity changes steadily over time: v = u + at. When thrown at an angle θ with speed u (ignoring air resistance), horizontal velocity u·cosθ stays constant while gravity slows and reverses the vertical velocity u·sinθ.",
                        standardTheory = "For constant acceleration: v = u + at, s = ut + ½at², v² = u² + 2as. In oblique projection from level ground: T = 2u·sinθ / g, H = u²·sin²θ / (2g), R = u²·sin(2θ) / g. Eliminating t yields the parabolic trajectory y = x·tanθ − gx² / (2u²·cos²θ) = x·tanθ·(1 − x/R).",
                        advancedTheory = "For variable acceleration a(x) or a(v), integrate v·dv = a(x)·dx or dt = dv/a(v). On an inclined plane of angle β, resolve g into g·cosβ (perpendicular to incline) and g·sinβ (along incline); maximum range up the incline is R_max = u² / [g(1 + sinβ)] at angle α = π/4 + β/2 with the horizontal.",
                        keyDefinitions = listOf(
                            "Displacement: Shortest directed vector from initial to final position (SI unit: m).",
                            "Instantaneous Velocity: Rate of change of position vector, v = dr/dt.",
                            "Range (R): Horizontal distance covered when projectile returns to launch level."
                        ),
                        derivationNote = "From x = (u·cosθ)t ⇒ t = x/(u·cosθ). Substituting into y = (u·sinθ)t − ½gt² gives y = x·tanθ − gx²/(2u²·cos²θ).",
                        solvedExamples = listOf(
                            SolvedExample(
                                problem = "A ball is projected at 20 m/s at 30° above the horizontal (g = 10 m/s²). Find maximum height and horizontal range.",
                                steps = listOf(
                                    "Vertical component u_y = 20 × sin30° = 10 m/s; horizontal u_x = 20 × cos30° = 10√3 m/s.",
                                    "Max height H = u_y² / (2g) = 10² / 20 = 5 m.",
                                    "Range R = u²·sin60° / g = 400 × (√3/2) / 10 = 20√3 ≈ 34.64 m."
                                ),
                                finalAnswer = "H = 5 m, R = 20√3 m"
                            )
                        ),
                        shortcuts = listOf(
                            "Complementary launch angles θ and (90° − θ) give identical horizontal range R.",
                            "Relation between height and range: tanθ = 4H / R."
                        ),
                        commonMistakes = listOf(
                            "Using v² = u² + 2as when acceleration is non-uniform (must use calculus instead).",
                            "Forgetting that speed at the highest point of a projectile is u·cosθ, NOT zero."
                        ),
                        examTips = listOf(
                            "Sketch velocity-time graphs first: area gives displacement, slope gives acceleration.",
                            if (adv) "In JEE Advanced, check inclined-plane frame rotation to avoid messy algebra." else "Use y = x·tanθ(1 − x/R) directly when heights at two x-values are given."
                        ),
                        revisionPoints = listOf(
                            "T = 2u·sinθ/g | H = u²·sin²θ/(2g) | R = u²·sin2θ/g",
                            "R_max = u²/g at θ = 45°; 4H = R·tanθ"
                        )
                    )
                )
            ),
            ChapterItem(
                id = "${exam.id}_phy_wpe",
                subjectId = phyId,
                examId = exam.id,
                order = 2,
                title = "Work, Power & Energy",
                introduction = "Connects forces to energy transfer through the Work-Energy Theorem, conservative potential fields, springs, and vertical circular motion.",
                learningObjectives = listOf(
                    "Compute work done by constant and variable forces via W = ∫ F·dr.",
                    "Apply the Work-Energy Theorem W_net = ΔK in inertial and non-inertial frames.",
                    "Analyze equilibrium stability using U(x) derivatives: dU/dx = 0 and d²U/dx²."
                ),
                importanceTag = "Must Master",
                estimatedMinutes = 50,
                topics = listOf(
                    TopicItem(
                        id = "${exam.id}_phy_wpe_t1",
                        chapterId = "${exam.id}_phy_wpe",
                        title = "Work-Energy Theorem & Conservative Forces",
                        subtopics = listOf("Kinetic energy", "Spring potential energy", "Equilibrium types"),
                        beginnerTheory = "Work happens when a force moves an object along its direction: W = F·d·cosθ. Kinetic energy K = ½mv² measures energy of motion. Total work done by all forces equals the change in kinetic energy.",
                        standardTheory = "W_all = ΔK = ½mv_f² − ½mv_i². For conservative forces (gravity, ideal spring, electrostatic), work is path-independent and F = −dU/dx. Mechanical energy E = K + U is conserved when non-conservative work (like friction) is zero.",
                        advancedTheory = "In 3D conservative fields, F = −∇U = −(∂U/∂x î + ∂U/∂y ĵ + ∂U/∂z k̂) and ∇ × F = 0. Equilibrium points satisfy dU/dx = 0: stable if d²U/dx² > 0, unstable if d²U/dx² < 0, neutral if d²U/dx² = 0.",
                        keyDefinitions = listOf(
                            "Kinetic Energy: K = ½mv² = p²/(2m) (SI unit: Joule).",
                            "Power: Rate of doing work, P = dW/dt = F·v (SI unit: Watt).",
                            "Conservative Force: Force whose work around any closed loop is zero."
                        ),
                        derivationNote = "Since F = m·dv/dt = m·v·(dv/dx), ∫F·dx = ∫m·v·dv = ½mv_f² − ½mv_i² = ΔK.",
                        solvedExamples = listOf(
                            SolvedExample(
                                problem = "A 2 kg block starts from rest on a smooth floor under F = 6x N. Find its speed at x = 2 m.",
                                steps = listOf(
                                    "Work W = ∫₀² 6x dx = [3x²]₀² = 3(4) = 12 J.",
                                    "By Work-Energy Theorem: ½mv² − 0 = 12 J ⇒ ½(2)v² = 12.",
                                    "v² = 12 ⇒ v = 2√3 ≈ 3.46 m/s."
                                ),
                                finalAnswer = "2√3 m/s"
                            )
                        ),
                        shortcuts = listOf(
                            "For a bob on a string of length L in a vertical circle: v_bottom ≥ √(5gL) to complete the loop.",
                            "Kinetic energy scales quadratically with momentum: K ∝ p² for constant mass."
                        ),
                        commonMistakes = listOf(
                            "Ignoring work done by pseudo-force when applying Work-Energy Theorem in an accelerating frame.",
                            "Assuming normal force always does zero work (false on moving wedges or elevators!)."
                        ),
                        examTips = listOf(
                            "Always list EVERY force (gravity, friction, spring, normal, external) before writing W_all = ΔK."
                        ),
                        revisionPoints = listOf(
                            "K = ½mv² = p²/2m | U_spring = ½kx² | F = −dU/dx",
                            "Minimum loop speed (string): √(5gL) at bottom, √(gL) at top."
                        )
                    )
                )
            ),
            ChapterItem(
                id = "${exam.id}_phy_electrostatics",
                subjectId = phyId,
                examId = exam.id,
                order = 3,
                title = "Electrostatics & Gauss's Law",
                introduction = "Covers Coulomb's Law, electric field and potential, dipoles, Gauss's Law symmetry applications, and conductors.",
                learningObjectives = listOf(
                    "Calculate electric fields and potentials for discrete and continuous charge distributions.",
                    "Apply Gauss's Law ∮ E·dA = q_enclosed / ε₀."
                ),
                importanceTag = "High Yield",
                estimatedMinutes = 60,
                topics = emptyList(),
                verificationStatus = VerificationStatus.AWAITING_VERIFIED
            ),

            // CHEMISTRY
            ChapterItem(
                id = "${exam.id}_chem_thermo",
                subjectId = chemId,
                examId = exam.id,
                order = 1,
                title = "Chemical Thermodynamics",
                introduction = "Governs energy conservation, enthalpy changes, entropy, Gibbs free energy, and spontaneity of chemical processes.",
                learningObjectives = listOf(
                    "Apply the First Law ΔU = q + w with IUPAC sign conventions.",
                    "Calculate isothermal reversible work and relate ΔH = ΔU + Δn_g·RT.",
                    "Predict spontaneity using ΔG = ΔH − TΔS and ΔG° = −RT·lnK."
                ),
                importanceTag = "High Yield",
                estimatedMinutes = 55,
                topics = listOf(
                    TopicItem(
                        id = "${exam.id}_chem_thermo_t1",
                        chapterId = "${exam.id}_chem_thermo",
                        title = "First Law, Enthalpy & Gibbs Free Energy",
                        subtopics = listOf("IUPAC work convention", "Hess's Law", "Spontaneity criterion"),
                        beginnerTheory = "Energy cannot be created or destroyed. If a system absorbs heat q and work w is done on it, its internal energy rises by ΔU = q + w. Reactions happen spontaneously at constant T and P when Gibbs energy decreases (ΔG < 0).",
                        standardTheory = "Under IUPAC convention, w = −P_ext·ΔV (irreversible) and w_rev = −nRT·ln(V₂/V₁) = −2.303·nRT·log₁₀(V₂/V₁) for an ideal gas isothermal process. Enthalpy H = U + PV gives ΔH = ΔU + Δn_g·RT. Spontaneity: ΔG = ΔH − TΔS < 0; at equilibrium ΔG = 0 and ΔG° = −RT·lnK_eq.",
                        advancedTheory = "For polytropic and adiabatic reversible paths of ideal gases, PV^γ = constant and T·V^(γ−1) = constant, with w_adiabatic = nR(T₂ − T₁)/(γ − 1) = nC_v(T₂ − T₁). Remember C_p − C_v = R and Kirchhoff's relation d(ΔH)/dT = ΔC_p.",
                        keyDefinitions = listOf(
                            "State Function: Property depending only on initial and final states (U, H, S, G), not path.",
                            "Enthalpy (H): Heat exchanged at constant pressure (q_p = ΔH).",
                            "Gibbs Free Energy (G): Maximum non-expansion useful work obtainable at constant T and P."
                        ),
                        derivationNote = "From H = U + PV at constant T for ideal gases: ΔH = ΔU + Δ(PV) = ΔU + Δn_g·RT.",
                        solvedExamples = listOf(
                            SolvedExample(
                                problem = "For a reaction at 300 K, ΔH = −20 kJ/mol and ΔS = −50 J/(mol·K). Is the reaction spontaneous?",
                                steps = listOf(
                                    "Convert ΔS to kJ/(mol·K): −50 J = −0.050 kJ/(mol·K).",
                                    "ΔG = ΔH − TΔS = −20 − (300 × −0.050) = −20 + 15 = −5 kJ/mol.",
                                    "Since ΔG < 0, the reaction is spontaneous at 300 K."
                                ),
                                finalAnswer = "Yes, spontaneous (ΔG = −5 kJ/mol)"
                            )
                        ),
                        shortcuts = listOf(
                            "ΔH < 0 and ΔS > 0 ⇒ always spontaneous at all temperatures.",
                            "Isothermal reversible expansion ALWAYS gives greater magnitude of work than irreversible expansion."
                        ),
                        commonMistakes = listOf(
                            "Mixing Joules (ΔS) with kiloJoules (ΔH) in ΔG = ΔH − TΔS!",
                            "Confusing Physics convention (ΔU = Q − W) with Chemistry IUPAC convention (ΔU = q + w)."
                        ),
                        examTips = listOf(
                            "Always check units of R (8.314 J/mol·K vs 0.0821 L·atm/mol·K) before plugging numbers."
                        ),
                        revisionPoints = listOf(
                            "ΔU = q + w | ΔH = ΔU + Δn_g·RT | ΔG = ΔH − TΔS",
                            "ΔG° = −2.303 RT log₁₀K"
                        )
                    )
                )
            ),
            ChapterItem(
                id = "${exam.id}_chem_bonding",
                subjectId = chemId,
                examId = exam.id,
                order = 2,
                title = "Chemical Bonding & Molecular Structure",
                introduction = "Explains VSEPR geometry, hybridization, Molecular Orbital Theory, dipole moments, and hydrogen bonding.",
                learningObjectives = listOf(
                    "Determine steric number, hybridization, and geometry via VSEPR.",
                    "Calculate bond order and magnetic character via Molecular Orbital Theory."
                ),
                importanceTag = "Must Master",
                estimatedMinutes = 50,
                topics = emptyList(),
                verificationStatus = VerificationStatus.AWAITING_VERIFIED
            ),

            // MATHEMATICS
            ChapterItem(
                id = "${exam.id}_math_calculus",
                subjectId = mathId,
                examId = exam.id,
                order = 1,
                title = "Limits, Differentiation & Definite Integration",
                introduction = "Core calculus toolkit covering indeterminate forms, derivatives, maxima/minima, and King's property in definite integrals.",
                learningObjectives = listOf(
                    "Evaluate limits using standard expansions and L'Hôpital's rule.",
                    "Apply product, quotient, chain, and implicit differentiation.",
                    "Use symmetry and King's property ∫ₐᵇ f(x)dx = ∫ₐᵇ f(a+b−x)dx."
                ),
                importanceTag = "Highest Weightage",
                estimatedMinutes = 65,
                topics = listOf(
                    TopicItem(
                        id = "${exam.id}_math_calc_t1",
                        chapterId = "${exam.id}_math_calculus",
                        title = "Standard Limits & Definite Integral Properties",
                        subtopics = listOf("0/0 & 1^∞ limits", "King's Property", "Leibniz differentiation rule"),
                        beginnerTheory = "A limit asks what value f(x) approaches as x gets arbitrarily close to a point a. A definite integral ∫ₐᵇ f(x)dx represents the signed area under y = f(x) from x = a to x = b.",
                        standardTheory = "Key limits: lim(x→0) sinx/x = 1, lim(x→0) (eˣ−1)/x = 1, lim(x→0) (1+x)^(1/x) = e. For 1^∞ forms, lim [f(x)]^g(x) = exp(lim g(x)·(f(x)−1)). King's Property: I = ∫ₐᵇ f(x)dx = ∫ₐᵇ f(a+b−x)dx. For even f on [−a, a], ∫ = 2∫₀ᵃ f(x)dx; for odd f, ∫ = 0.",
                        advancedTheory = "Newton-Leibniz Rule: d/dx [∫_{g(x)}^{h(x)} f(t)dt] = f(h(x))·h'(x) − f(g(x))·g'(x). Combine with Taylor series (sinx = x − x³/6 + ..., cosx = 1 − x²/2 + ..., ln(1+x) = x − x²/2 + x³/3 − ...) for rapid evaluation of high-order limits.",
                        keyDefinitions = listOf(
                            "Indeterminate Forms: 0/0, ∞/∞, 0×∞, ∞−∞, 1^∞, 0⁰, ∞⁰.",
                            "King's Property: ∫ₐᵇ f(x)dx = ∫ₐᵇ f(a+b−x)dx."
                        ),
                        derivationNote = "Put t = a + b − x ⇒ dx = −dt. Limits swap from (a,b) to (b,a), cancelling the negative sign to give ∫ₐᵇ f(a+b−x)dt.",
                        solvedExamples = listOf(
                            SolvedExample(
                                problem = "Evaluate I = ∫₀^(π/2) [sinx / (sinx + cosx)] dx.",
                                steps = listOf(
                                    "Apply King's property replacing x with π/2 − x: sin(π/2 − x) = cosx, cos(π/2 − x) = sinx.",
                                    "So I = ∫₀^(π/2) [cosx / (cosx + sinx)] dx.",
                                    "Add both expressions: 2I = ∫₀^(π/2) 1 dx = π/2 ⇒ I = π/4."
                                ),
                                finalAnswer = "π/4"
                            )
                        ),
                        shortcuts = listOf(
                            "∫ₐᵇ f(x) / [f(x) + f(a+b−x)] dx = (b − a) / 2 ALWAYS!",
                            "For 1^∞ limit: directly compute e^(lim (base − 1) × exponent)."
                        ),
                        commonMistakes = listOf(
                            "Applying L'Hôpital's rule without checking whether the limit is actually 0/0 or ∞/∞.",
                            "Forgetting to check discontinuity inside [a, b] before applying Fundamental Theorem of Calculus."
                        ),
                        examTips = listOf(
                            "Whenever limits are symmetric [−a, a] or [0, π/2], test odd/even or King's property first!"
                        ),
                        revisionPoints = listOf(
                            "∫ₐᵇ f(x)/[f(x)+f(a+b−x)] dx = (b−a)/2",
                            "Newton-Leibniz: d/dx ∫_{g(x)}^{h(x)} f(t)dt = f(h)h' − f(g)g'"
                        )
                    )
                )
            ),
            ChapterItem(
                id = "${exam.id}_math_quad",
                subjectId = mathId,
                examId = exam.id,
                order = 2,
                title = "Quadratic Equations & Matrices",
                introduction = "Analyzes nature of roots, Vieta's formulas, location of roots, determinants, and matrix inverses.",
                learningObjectives = listOf(
                    "Use discriminant D = b² − 4ac and sum/product of roots.",
                    "Apply Cayley-Hamilton theorem and adjoint properties for matrices."
                ),
                importanceTag = "High Yield",
                estimatedMinutes = 45,
                topics = emptyList(),
                verificationStatus = VerificationStatus.AWAITING_VERIFIED
            )
        )
    }

    fun formulas(exam: ExamSystem): List<FormulaItem> {
        val phyId = "${exam.id}_phy"
        val chemId = "${exam.id}_chem"
        val mathId = "${exam.id}_math"
        return listOf(
            FormulaItem(
                id = "${exam.id}_f_ke",
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_wpe",
                chapterName = "Work, Power & Energy",
                topicName = "Kinetic Energy",
                category = "Mechanics",
                title = "Kinetic Energy & Momentum Relation",
                expression = "K = ½ m v² = p² / (2m)",
                variables = mapOf("K" to "Kinetic energy", "m" to "Mass of body", "v" to "Speed", "p" to "Linear momentum (mv)"),
                units = "Joules (J) = kg·m²/s²",
                explanation = "Translational kinetic energy of a non-relativistic body moving at speed v, also expressible via momentum p.",
                conditionsOfUse = "Valid for classical mechanics (v ≪ c) and rigid-body translation.",
                example = "A 4 kg block moving at 5 m/s has K = ½ × 4 × 25 = 50 J.",
                relatedFormulaIds = listOf("${exam.id}_f_wet"),
                keywords = listOf("kinetic energy", "momentum", "mechanics", "velocity", "energy")
            ),
            FormulaItem(
                id = "${exam.id}_f_wet",
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_wpe",
                chapterName = "Work, Power & Energy",
                topicName = "Work-Energy Theorem",
                category = "Mechanics",
                title = "Work-Energy Theorem",
                expression = "W_net = ΔK = ½ m v_f² − ½ m v_i²",
                variables = mapOf("W_net" to "Sum of work done by ALL forces", "ΔK" to "Change in kinetic energy"),
                units = "Joules (J)",
                explanation = "The net work performed by conservative, non-conservative, and external forces equals the change in kinetic energy.",
                conditionsOfUse = "Valid in inertial frames; include pseudo-force work in accelerating frames.",
                example = "If net work is 40 J on a body starting from rest, final K = 40 J.",
                relatedFormulaIds = listOf("${exam.id}_f_ke"),
                keywords = listOf("work", "energy", "theorem", "kinetic")
            ),
            FormulaItem(
                id = "${exam.id}_f_proj_range",
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_kinematics",
                chapterName = "Kinematics & Projectile Motion",
                topicName = "Projectile Motion",
                category = "Kinematics",
                title = "Horizontal Range of Projectile",
                expression = "R = (u² · sin 2θ) / g",
                variables = mapOf("R" to "Horizontal range", "u" to "Launch speed", "θ" to "Angle with horizontal", "g" to "Gravitational acceleration"),
                units = "Meters (m)",
                explanation = "Total horizontal distance traveled before returning to launch elevation.",
                conditionsOfUse = "Flat ground, constant uniform gravity, negligible air drag.",
                example = "u = 20 m/s, θ = 45°, g = 10 m/s² ⇒ R = 400 × 1 / 10 = 40 m.",
                keywords = listOf("projectile", "range", "kinematics", "angle", "trajectory")
            ),
            FormulaItem(
                id = "${exam.id}_f_coulomb",
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_electrostatics",
                chapterName = "Electrostatics & Gauss's Law",
                topicName = "Coulomb's Law",
                category = "Electrodynamics",
                title = "Coulomb's Electrostatic Force",
                expression = "F = (1 / 4πε₀) · (|q₁ q₂| / r²)",
                variables = mapOf("F" to "Electrostatic force", "q₁, q₂" to "Point charges", "r" to "Separation distance", "1/4πε₀" to "9 × 10⁹ N·m²/C²"),
                units = "Newtons (N)",
                explanation = "Magnitude of force between two stationary point charges in vacuum.",
                conditionsOfUse = "Point charges or spherically symmetric distributions at rest in vacuum.",
                example = "Two 1 μC charges 1 m apart exert F = 9 × 10⁻³ N.",
                keywords = listOf("coulomb", "charge", "electrostatics", "electric force")
            ),
            FormulaItem(
                id = "${exam.id}_f_gibbs",
                examId = exam.id,
                subjectId = chemId,
                subjectName = "Chemistry",
                chapterId = "${exam.id}_chem_thermo",
                chapterName = "Chemical Thermodynamics",
                topicName = "Gibbs Free Energy",
                category = "Physical Chemistry",
                title = "Gibbs-Helmholtz Spontaneity Equation",
                expression = "ΔG = ΔH − TΔS   |   ΔG° = −2.303 R T log₁₀ K",
                variables = mapOf("ΔG" to "Gibbs energy change", "ΔH" to "Enthalpy change", "T" to "Absolute temperature (K)", "ΔS" to "Entropy change", "K" to "Equilibrium constant"),
                units = "kJ/mol (ensure TΔS is converted to kJ/mol)",
                explanation = "Determines spontaneity at constant temperature and pressure (ΔG < 0 spontaneous).",
                conditionsOfUse = "Constant temperature and constant pressure closed system.",
                example = "If ΔH = −30 kJ/mol, T = 300 K, ΔS = −0.05 kJ/mol·K ⇒ ΔG = −15 kJ/mol.",
                keywords = listOf("gibbs", "free energy", "thermodynamics", "entropy", "spontaneous", "enthalpy")
            ),
            FormulaItem(
                id = "${exam.id}_f_enthalpy",
                examId = exam.id,
                subjectId = chemId,
                subjectName = "Chemistry",
                chapterId = "${exam.id}_chem_thermo",
                chapterName = "Chemical Thermodynamics",
                topicName = "Enthalpy & Internal Energy",
                category = "Physical Chemistry",
                title = "Enthalpy-Internal Energy Relation",
                expression = "ΔH = ΔU + Δn_g R T",
                variables = mapOf("ΔH" to "Enthalpy change", "ΔU" to "Internal energy change", "Δn_g" to "Moles of gaseous products − gaseous reactants", "R" to "8.314 J/(mol·K)"),
                units = "J/mol or kJ/mol",
                explanation = "Relates heat at constant pressure (ΔH) to heat at constant volume (ΔU) for reactions involving ideal gases.",
                conditionsOfUse = "Ideal gas behavior at constant temperature T; ignore solids and liquids in Δn_g.",
                example = "For N₂(g) + 3H₂(g) → 2NH₃(g), Δn_g = 2 − 4 = −2.",
                keywords = listOf("enthalpy", "internal energy", "delta h", "thermodynamics")
            ),
            FormulaItem(
                id = "${exam.id}_f_king",
                examId = exam.id,
                subjectId = mathId,
                subjectName = "Mathematics",
                chapterId = "${exam.id}_math_calculus",
                chapterName = "Limits, Differentiation & Definite Integration",
                topicName = "Definite Integration",
                category = "Calculus",
                title = "King's Property of Definite Integrals",
                expression = "∫ₐᵇ f(x) dx = ∫ₐᵇ f(a + b − x) dx",
                variables = mapOf("a, b" to "Lower and upper limits of integration", "f(x)" to "Integrable function on [a, b]"),
                units = "Dimensionless / Area units",
                explanation = "Reversing the variable across the interval [a, b] preserves the definite integral and often simplifies trigonometric/rational integrands.",
                conditionsOfUse = "f(x) must be integrable on [a, b].",
                example = "∫₀^(π/2) sinx/(sinx+cosx) dx = π/4 via adding I + I.",
                keywords = listOf("king property", "definite integral", "integration", "calculus")
            ),
            FormulaItem(
                id = "${exam.id}_f_quad",
                examId = exam.id,
                subjectId = mathId,
                subjectName = "Mathematics",
                chapterId = "${exam.id}_math_quad",
                chapterName = "Quadratic Equations & Matrices",
                topicName = "Quadratic Roots",
                category = "Algebra",
                title = "Quadratic Formula & Vieta's Relations",
                expression = "x = [−b ± √(b² − 4ac)] / (2a)   |   α+β = −b/a, αβ = c/a",
                variables = mapOf("a, b, c" to "Coefficients of ax² + bx + c = 0 (a ≠ 0)", "α, β" to "Roots of equation"),
                units = "Dimensionless",
                explanation = "Gives exact roots and symmetric sum/product identities for any quadratic equation.",
                conditionsOfUse = "a ≠ 0; real roots iff discriminant D = b² − 4ac ≥ 0.",
                example = "For x² − 5x + 6 = 0, α+β = 5, αβ = 6 ⇒ roots are 2 and 3.",
                keywords = listOf("quadratic", "discriminant", "roots", "vieta", "algebra")
            )
        )
    }

    fun questions(exam: ExamSystem): List<QuestionItem> {
        val phyId = "${exam.id}_phy"
        val chemId = "${exam.id}_chem"
        val mathId = "${exam.id}_math"
        val raw = listOf(
            QuestionItem(
                id = "${exam.id}_q1",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("projectile angle 4H equals R"),
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_kinematics",
                chapterName = "Kinematics & Projectile Motion",
                topicId = "${exam.id}_phy_kin_t1",
                topicName = "Projectile Trajectory",
                difficulty = QuestionDifficulty.EASY,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "A projectile is fired from level ground such that its horizontal range R is equal to 4 times its maximum height H. What is the angle of projection with the horizontal?",
                options = listOf("30°", "45°", "60°", "tan⁻¹(2)"),
                correctAnswer = "45°",
                solutionSteps = listOf(
                    "Recall the standard identity connecting max height H and range R: tanθ = 4H / R.",
                    "Given R = 4H ⇒ 4H / R = 1.",
                    "Therefore tanθ = 1 ⇒ θ = 45°."
                ),
                explanation = "At 45°, both sin(2θ) is maximized and 4H/R = tan(45°) = 1.",
                relatedFormula = "tanθ = 4H / R"
            ),
            QuestionItem(
                id = "${exam.id}_q2",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("kinetic energy momentum increases by 100 percent"),
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_wpe",
                chapterName = "Work, Power & Energy",
                topicId = "${exam.id}_phy_wpe_t1",
                topicName = "Kinetic Energy & Momentum",
                difficulty = QuestionDifficulty.EASY,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "If the linear momentum of a moving body of constant mass increases by 100%, its kinetic energy increases by:",
                options = listOf("100%", "200%", "300%", "400%"),
                correctAnswer = "300%",
                solutionSteps = listOf(
                    "Use K = p² / (2m). Since mass m is constant, K ∝ p².",
                    "A 100% increase means p_new = 2p.",
                    "K_new = (2p)² / (2m) = 4K. Increase = (4K − K)/K × 100% = 300%."
                ),
                explanation = "Students often pick 400%, which is the new value (4K), not the percentage increase (3K).",
                relatedFormula = "K = p² / (2m)"
            ),
            QuestionItem(
                id = "${exam.id}_q3",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("variable force 6x work energy speed 2kg"),
                examId = exam.id,
                subjectId = phyId,
                subjectName = "Physics",
                chapterId = "${exam.id}_phy_wpe",
                chapterName = "Work, Power & Energy",
                topicId = "${exam.id}_phy_wpe_t1",
                topicName = "Work-Energy Theorem",
                difficulty = QuestionDifficulty.MEDIUM,
                questionType = QuestionType.NUMERICAL,
                questionText = "A 2 kg particle starts from rest at x = 0 under a net force F(x) = 4x N along the x-axis. Find its speed (in m/s) when it reaches x = 3 m.",
                options = listOf("3", "6", "9", "18"),
                correctAnswer = "6",
                solutionSteps = listOf(
                    "Compute work W = ∫₀³ 4x dx = [2x²]₀³ = 2(9) = 18 J.",
                    "By Work-Energy Theorem: ½mv² = 18 J ⇒ ½(2)v² = 18 ⇒ v² = 18? Wait: let's re-evaluate carefully — if F = 4x, ∫₀³ 4x dx = 18, v = √18. Let's make F(x) = 8x so v = 6 exactly!"
                ).let {
                    listOf(
                        "Work done W = ∫₀³ 8x dx = [4x²]₀³ = 4 × 9 = 36 J.",
                        "By Work-Energy Theorem: ½(2)v² = 36 J ⇒ v² = 36.",
                        "Hence v = 6 m/s."
                    )
                },
                explanation = "Integrating F(x) gives total work directly, avoiding time-dependent kinematics.",
                relatedFormula = "W = ∫ F(x) dx = ½mv² − ½mu²"
            ).copy(
                questionText = "A 2 kg particle starts from rest at x = 0 under a net force F(x) = 8x N along the x-axis. Find its speed (in m/s) when it reaches x = 3 m."
            ),
            QuestionItem(
                id = "${exam.id}_q4",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("thermodynamics delta H delta U delta ng RT NH3"),
                examId = exam.id,
                subjectId = chemId,
                subjectName = "Chemistry",
                chapterId = "${exam.id}_chem_thermo",
                chapterName = "Chemical Thermodynamics",
                topicId = "${exam.id}_chem_thermo_t1",
                topicName = "Enthalpy Relation",
                difficulty = QuestionDifficulty.EASY,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "For the gaseous reaction N₂(g) + 3H₂(g) → 2NH₃(g) at constant temperature T, the value of (ΔH − ΔU) is:",
                options = listOf("+2RT", "−2RT", "−RT", "+RT"),
                correctAnswer = "−2RT",
                solutionSteps = listOf(
                    "Recall ΔH − ΔU = Δn_g·RT.",
                    "Gaseous moles of products = 2; gaseous moles of reactants = 1 + 3 = 4.",
                    "Δn_g = 2 − 4 = −2 ⇒ ΔH − ΔU = −2RT."
                ),
                explanation = "Only gaseous stoichiometric coefficients contribute to Δn_g.",
                relatedFormula = "ΔH = ΔU + Δn_g·RT"
            ),
            QuestionItem(
                id = "${exam.id}_q5",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("spontaneous reaction delta H positive delta S positive"),
                examId = exam.id,
                subjectId = chemId,
                subjectName = "Chemistry",
                chapterId = "${exam.id}_chem_thermo",
                chapterName = "Chemical Thermodynamics",
                topicId = "${exam.id}_chem_thermo_t1",
                topicName = "Gibbs Free Energy",
                difficulty = QuestionDifficulty.MEDIUM,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "An endothermic reaction (ΔH > 0) with positive entropy change (ΔS > 0) is spontaneous:",
                options = listOf(
                    "At all temperatures",
                    "Never at any temperature",
                    "At sufficiently high temperatures (T > ΔH/ΔS)",
                    "Only at sufficiently low temperatures (T < ΔH/ΔS)"
                ),
                correctAnswer = "At sufficiently high temperatures (T > ΔH/ΔS)",
                solutionSteps = listOf(
                    "Spontaneity at constant T and P requires ΔG = ΔH − TΔS < 0.",
                    "Rearranging gives TΔS > ΔH.",
                    "Since ΔS > 0, this holds whenever T > ΔH / ΔS (high temperatures)."
                ),
                explanation = "Entropy-driven reactions overcome a positive enthalpy barrier when T is large enough.",
                relatedFormula = "ΔG = ΔH − TΔS < 0"
            ),
            QuestionItem(
                id = "${exam.id}_q6",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("definite integral 0 to pi/2 sinx over sinx plus cosx"),
                examId = exam.id,
                subjectId = mathId,
                subjectName = "Mathematics",
                chapterId = "${exam.id}_math_calculus",
                chapterName = "Limits, Differentiation & Definite Integration",
                topicId = "${exam.id}_math_calc_t1",
                topicName = "Definite Integration",
                difficulty = QuestionDifficulty.MEDIUM,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "The value of the definite integral ∫₀^(π/2) [ √(sin x) / (√(sin x) + √(cos x)) ] dx is:",
                options = listOf("π / 2", "π / 4", "π / 6", "1"),
                correctAnswer = "π / 4",
                solutionSteps = listOf(
                    "Let I = ∫₀^(π/2) [ √(sin x) / (√(sin x) + √(cos x)) ] dx.",
                    "Apply King's property x → π/2 − x: I = ∫₀^(π/2) [ √(cos x) / (√(cos x) + √(sin x)) ] dx.",
                    "Adding both: 2I = ∫₀^(π/2) 1 dx = π/2 ⇒ I = π/4."
                ),
                explanation = "Any integral of the form ∫ₐᵇ f(x)/[f(x)+f(a+b−x)] dx evaluates directly to (b−a)/2.",
                relatedFormula = "∫ₐᵇ f(x)dx = ∫ₐᵇ f(a+b−x)dx"
            ),
            QuestionItem(
                id = "${exam.id}_q7",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("limit x to 0 (1 plus 3x) power 2 over x"),
                examId = exam.id,
                subjectId = mathId,
                subjectName = "Mathematics",
                chapterId = "${exam.id}_math_calculus",
                chapterName = "Limits, Differentiation & Definite Integration",
                topicId = "${exam.id}_math_calc_t1",
                topicName = "Standard Limits",
                difficulty = QuestionDifficulty.MEDIUM,
                questionType = QuestionType.SINGLE_CHOICE,
                questionText = "Evaluate lim (x → 0) (1 + 3x)^(2/x):",
                options = listOf("e³", "e⁵", "e⁶", "e^(3/2)"),
                correctAnswer = "e⁶",
                solutionSteps = listOf(
                    "Recognize the 1^∞ indeterminate form as x → 0.",
                    "Apply lim [f(x)]^g(x) = exp(lim g(x) · (f(x) − 1)).",
                    "Exponent = lim (x → 0) (2/x)·(3x) = 6 ⇒ Limit = e⁶."
                ),
                explanation = "The 1^∞ exponential shortcut saves significant time over logarithmic expansion.",
                relatedFormula = "lim (1 + ax)^(b/x) = e^(ab)"
            ),
            QuestionItem(
                id = "${exam.id}_q8",
                fingerprint = FirebaseMastreyAiService.computeQuestionFingerprint("quadratic equation sum of squares of roots x2 - 6x + 4"),
                examId = exam.id,
                subjectId = mathId,
                subjectName = "Mathematics",
                chapterId = "${exam.id}_math_quad",
                chapterName = "Quadratic Equations & Matrices",
                topicId = "${exam.id}_math_quad_t1",
                topicName = "Quadratic Roots",
                difficulty = QuestionDifficulty.HARD,
                questionType = QuestionType.NUMERICAL,
                questionText = "If α and β are the roots of x² − 6x + 4 = 0, find the value of α² + β².",
                options = listOf("20", "28", "32", "44"),
                correctAnswer = "28",
                solutionSteps = listOf(
                    "From Vieta's relations: α + β = 6 and αβ = 4.",
                    "Use identity α² + β² = (α + β)² − 2αβ.",
                    "α² + β² = 6² − 2(4) = 36 − 8 = 28."
                ),
                explanation = "Never compute irrational roots explicitly when a symmetric expression can be written via (α+β) and αβ.",
                relatedFormula = "α² + β² = (α + β)² − 2αβ"
            )
        )
        return raw
    }

    fun flashcards(exam: ExamSystem): List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "${exam.id}_fc1",
            examId = exam.id,
            subjectId = "${exam.id}_phy",
            subjectName = "Physics",
            chapterId = "${exam.id}_phy_wpe",
            chapterName = "Work, Power & Energy",
            category = FlashcardCategory.FORMULA,
            difficulty = QuestionDifficulty.EASY,
            front = "Minimum bottom speed for a mass tied to a string of length L to complete a vertical circle?",
            back = "v_bottom = √(5gL), with v_top = √(gL) where string tension becomes zero.",
            hint = "Apply energy conservation between bottom and top with T_top ≥ 0."
        ),
        FlashcardItem(
            id = "${exam.id}_fc2",
            examId = exam.id,
            subjectId = "${exam.id}_chem",
            subjectName = "Chemistry",
            chapterId = "${exam.id}_chem_thermo",
            chapterName = "Chemical Thermodynamics",
            category = FlashcardCategory.CONCEPT,
            difficulty = QuestionDifficulty.MEDIUM,
            front = "When is a reaction spontaneous at ALL temperatures under constant T and P?",
            back = "When ΔH < 0 (exothermic) AND ΔS > 0 (entropy increases), guaranteeing ΔG = ΔH − TΔS < 0 for every T > 0.",
            hint = "Check the signs of both terms in ΔG = ΔH − TΔS."
        ),
        FlashcardItem(
            id = "${exam.id}_fc3",
            examId = exam.id,
            subjectId = "${exam.id}_math",
            subjectName = "Mathematics",
            chapterId = "${exam.id}_math_calculus",
            chapterName = "Limits, Differentiation & Definite Integration",
            category = FlashcardCategory.FORMULA,
            difficulty = QuestionDifficulty.MEDIUM,
            front = "State the Newton-Leibniz rule for differentiating an integral with variable limits.",
            back = "d/dx ∫_{g(x)}^{h(x)} f(t) dt = f(h(x))·h'(x) − f(g(x))·g'(x)",
            hint = "Substitute upper limit times its derivative minus lower limit times its derivative."
        )
    )
}
