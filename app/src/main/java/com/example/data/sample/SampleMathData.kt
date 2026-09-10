package com.example.data.sample

import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.SpecialContentEntity
import com.example.data.database.entity.TopicEntity

object SampleMathData {

    val topics = listOf(
        // Regular Categories
        TopicEntity("alg_linear", "Algebra", "Linear Equations & Inequalities", "Solving first-degree systems, coordinate lines, and inequalities", 1, "functions", false, 50),
        TopicEntity("alg_quadratic", "Algebra", "Quadratic Equations & Polynomials", "Roots, Vieta's formulas, discriminants, and factoring", 2, "square_foot", false, 65),
        TopicEntity("trig_identities", "Trigonometry", "Trigonometric Identities & Values", "Pythagorean identities, angle additions, and reductions", 3, "change_history", false, 40),
        TopicEntity("trig_equations", "Trigonometry", "Trigonometric Equations", "Solving equations with sine, cosine, tangent functions", 4, "gesture", false, 35),
        TopicEntity("geom_triangles", "Geometry", "Triangles & Euclidean Theorems", "Ceva, Menelaus, circumcircle, incircle, and similarity", 5, "architecture", false, 60),
        TopicEntity("geom_circles", "Geometry", "Circles & Tangents", "Power of a point, inscribed angles, and chord lengths", 6, "circle", false, 45),
        TopicEntity("num_divisibility", "Number Theory", "Divisibility & Prime Numbers", "GCD, LCM, Euclidean algorithm, and modular arithmetic", 7, "pin", false, 55),
        TopicEntity("num_diophantine", "Number Theory", "Diophantine Equations", "Linear and non-linear integer solutions", 8, "calculate", false, 30),
        TopicEntity("comb_permutations", "Combinatorics", "Permutations & Combinations", "Arrangements, binomial coefficients, and combinations", 9, "dashboard_customize", false, 45),
        TopicEntity("prob_classical", "Probability", "Classical & Conditional Probability", "Independent events, Bayes theorem, and expectation", 10, "casino", false, 40),
        TopicEntity("stat_analysis", "Statistics", "Descriptive Statistics & Variance", "Mean, median, mode, standard deviation, and distributions", 11, "bar_chart", false, 30),
        TopicEntity("calc_derivatives", "Calculus", "Limits & Derivatives", "Continuity, product rule, chain rule, and extrema", 12, "trending_up", false, 50),
        TopicEntity("calc_integrals", "Calculus", "Definite & Indefinite Integrals", "Substitution, integration by parts, and area under curve", 13, "area_chart", false, 45),
        TopicEntity("logic_boolean", "Logic", "Mathematical Logic & Sets", "Truth tables, implications, Venn diagrams, and quantifiers", 14, "rule", false, 35),
        TopicEntity("olympiad_inequalities", "Olympiad", "Olympiad Inequalities", "AM-GM, Cauchy-Schwarz, Jensen, and rearrangement", 15, "military_tech", false, 40),

        // MAXSUS Special Topics (Prepared for Usmonov book integration)
        TopicEntity("maxsus_usmonov_1", "MAXSUS", "Usmonov: Algebra Asoslari (Maxsus)", "Maxsus mavzular bo'yicha maxsus nazariya va testlar", 1, "auto_stories", true, 20),
        TopicEntity("maxsus_usmonov_2", "MAXSUS", "Usmonov: Tenglamalar va Tengsizliklar", "Murakkab ildizlar, modul va nostandart usullar", 2, "menu_book", true, 25),
        TopicEntity("maxsus_usmonov_3", "MAXSUS", "Usmonov: Geometriya va Trigonometriya", "Chizma masalalari va geometrik isbotlar", 3, "psychology", true, 20)
    )

    val problems = listOf(
        // ALGEBRA
        ProblemEntity(
            id = "prob_alg_001",
            category = "Algebra",
            topicId = "alg_linear",
            difficulty = "EASY",
            question = "Solve for x: 5x - 7 = 3x + 9",
            optionA = "x = 4",
            optionB = "x = 8",
            optionC = "x = 16",
            optionD = "x = 2",
            correctAnswer = "B",
            solution = "Subtract 3x from both sides: 2x - 7 = 9. Add 7 to both sides: 2x = 16. Divide by 2: x = 8.",
            explanation = "Isolate the variable term on one side of the equality.",
            formula = "ax + b = cx + d => (a - c)x = d - b",
            tags = "linear,equations,algebra",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_alg_linear_001"
        ),
        ProblemEntity(
            id = "prob_alg_002",
            category = "Algebra",
            topicId = "alg_linear",
            difficulty = "MEDIUM",
            question = "For what value of k does the system of equations have infinitely many solutions?\n2x + 3y = 7\n4x + ky = 14",
            optionA = "k = 3",
            optionB = "k = 6",
            optionC = "k = 12",
            optionD = "k = 1.5",
            correctAnswer = "B",
            solution = "For infinite solutions, the coefficients must be proportional: 2/4 = 3/k = 7/14. Since 2/4 = 1/2, 3/k = 1/2 gives k = 6.",
            explanation = "Two lines are coincident when their corresponding coefficients have equal ratios.",
            formula = "a1/a2 = b1/b2 = c1/c2",
            tags = "systems,linear,ratios",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_alg_linear_002"
        ),
        ProblemEntity(
            id = "prob_alg_003",
            category = "Algebra",
            topicId = "alg_quadratic",
            difficulty = "MEDIUM",
            question = "If r and s are the roots of 2x² - 6x + 1 = 0, find the value of (1/r) + (1/s).",
            optionA = "6",
            optionB = "3",
            optionC = "1/6",
            optionD = "12",
            correctAnswer = "A",
            solution = "By Vieta's formulas: r + s = -(-6)/2 = 3, and r · s = 1/2.\nThen (1/r) + (1/s) = (r + s) / (r · s) = 3 / (1/2) = 6.",
            explanation = "Express the required symmetric expression in terms of sum and product of roots.",
            formula = "(1/r) + (1/s) = (r + s) / (rs)",
            tags = "vieta,roots,quadratics",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_alg_quad_003"
        ),
        ProblemEntity(
            id = "prob_alg_004",
            category = "Algebra",
            topicId = "alg_quadratic",
            difficulty = "HARD",
            question = "Find the sum of all real values of x satisfying: (x² - 5x + 5)^(x² - 9x + 20) = 1",
            optionA = "14",
            optionB = "17",
            optionC = "15",
            optionD = "10",
            correctAnswer = "C",
            solution = "Case 1: Base = 1 => x² - 5x + 5 = 1 => x² - 5x + 4 = 0 => x = 1, x = 4.\nCase 2: Exponent = 0 and Base ≠ 0 => x² - 9x + 20 = 0 => x = 4, x = 5 (for x=5, base=5≠0; for x=4, base=1≠0).\nCase 3: Base = -1 and Exponent is even => x² - 5x + 5 = -1 => x² - 5x + 6 = 0 => x = 2, x = 3.\nCheck parity of exponent for x=2: 2² - 18 + 20 = 6 (even, valid!).\nFor x=3: 3² - 27 + 20 = 2 (even, valid!).\nDistinct real solutions are x ∈ {1, 2, 3, 4, 5}. Sum = 1 + 2 + 3 + 4 + 5 = 15.",
            explanation = "Three distinct conditions satisfy a^b = 1: base = 1, exponent = 0 with base ≠ 0, or base = -1 with even exponent.",
            formula = "a^b = 1 iff (a=1) OR (b=0, a≠0) OR (a=-1, b is even)",
            tags = "exponential,polynomials,olympiad",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_alg_quad_004"
        ),

        // TRIGONOMETRY
        ProblemEntity(
            id = "prob_trig_001",
            category = "Trigonometry",
            topicId = "trig_identities",
            difficulty = "EASY",
            question = "Evaluate: sin²(23°) + sin²(67°)",
            optionA = "0",
            optionB = "1",
            optionC = "0.5",
            optionD = "2",
            correctAnswer = "B",
            solution = "Since 67° = 90° - 23°, sin(67°) = cos(23°). Therefore sin²(23°) + cos²(23°) = 1.",
            explanation = "Complementary angle identity: sin(90° - θ) = cos(θ).",
            formula = "sin²(θ) + cos²(θ) = 1",
            tags = "trigonometry,identities",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_trig_001"
        ),
        ProblemEntity(
            id = "prob_trig_002",
            category = "Trigonometry",
            topicId = "trig_equations",
            difficulty = "MEDIUM",
            question = "How many solutions does sin(2x) = cos(x) have in the interval [0, 2π)?",
            optionA = "2",
            optionB = "3",
            optionC = "4",
            optionD = "6",
            correctAnswer = "C",
            solution = "2 sin(x) cos(x) - cos(x) = 0 => cos(x) (2 sin(x) - 1) = 0.\nEither cos(x) = 0 => x = π/2, 3π/2.\nOr sin(x) = 1/2 => x = π/6, 5π/6.\nTotal of 4 solutions in [0, 2π).",
            explanation = "Use the double angle identity sin(2x) = 2 sin(x) cos(x) and factor out cos(x).",
            formula = "sin(2x) = 2 sin(x) cos(x)",
            tags = "trig,equations",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_trig_002"
        ),

        // GEOMETRY
        ProblemEntity(
            id = "prob_geom_001",
            category = "Geometry",
            topicId = "geom_triangles",
            difficulty = "EASY",
            question = "In a right triangle, the legs have lengths 9 and 12. Find the length of the altitude to the hypotenuse.",
            optionA = "7.2",
            optionB = "6.0",
            optionC = "8.4",
            optionD = "7.5",
            correctAnswer = "A",
            solution = "Hypotenuse = √(9² + 12²) = √(81 + 144) = 15.\nArea of triangle = (1/2) · 9 · 12 = 54.\nAlso Area = (1/2) · 15 · h => 54 = 7.5 · h => h = 54 / 7.5 = 7.2.",
            explanation = "Equate the area of the right triangle computed in two different orientations.",
            formula = "h = (a · b) / c",
            tags = "geometry,triangles,pythagoras",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_geom_001"
        ),
        ProblemEntity(
            id = "prob_geom_002",
            category = "Geometry",
            topicId = "geom_circles",
            difficulty = "HARD",
            question = "From an external point P, a tangent PT of length 8 is drawn to a circle. A secant through P intersects the circle at A and B such that PA = 4. What is the length of chord AB?",
            optionA = "12",
            optionB = "16",
            optionC = "8",
            optionD = "10",
            correctAnswer = "A",
            solution = "By Power of a Point theorem: PT² = PA · PB.\n8² = 4 · PB => 64 = 4 · PB => PB = 16.\nSince PB = PA + AB => 16 = 4 + AB => AB = 12.",
            explanation = "The tangent-secant theorem states that the square of the tangent equals the product of the external secant segment and entire secant.",
            formula = "PT² = PA · PB",
            tags = "geometry,circles,power_of_point",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_geom_002"
        ),

        // NUMBER THEORY
        ProblemEntity(
            id = "prob_num_001",
            category = "Number Theory",
            topicId = "num_divisibility",
            difficulty = "MEDIUM",
            question = "What is the remainder when 3^2024 is divided by 7?",
            optionA = "1",
            optionB = "2",
            optionC = "4",
            optionD = "6",
            correctAnswer = "B",
            solution = "By Fermat's Little Theorem, 3^(7-1) ≡ 3^6 ≡ 1 (mod 7).\n2024 = 6 × 337 + 2.\nSo 3^2024 = (3^6)^337 · 3² ≡ 1^337 · 9 ≡ 9 ≡ 2 (mod 7).",
            explanation = "Use modular reduction via Fermat's Little Theorem with prime modulus 7.",
            formula = "a^(p-1) ≡ 1 (mod p)",
            tags = "number_theory,modular_arithmetic,fermat",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_num_001"
        ),
        ProblemEntity(
            id = "prob_num_002",
            category = "Number Theory",
            topicId = "num_diophantine",
            difficulty = "HARD",
            question = "Find the number of pairs of positive integers (x, y) satisfying: (1/x) + (1/y) = 1/12",
            optionA = "8",
            optionB = "7",
            optionC = "15",
            optionD = "9",
            correctAnswer = "C",
            solution = "(1/x) + (1/y) = 1/12 => 12(x + y) = xy => xy - 12x - 12y + 144 = 144 => (x - 12)(y - 12) = 144.\nSince x, y > 12, (x - 12) must be a positive divisor of 144 = 2^4 · 3².\nTotal number of positive divisors = (4 + 1)(2 + 1) = 5 × 3 = 15.",
            explanation = "Simon's Favorite Factoring Trick transforms reciprocal Diophantine equations into divisor counting problems.",
            formula = "(x - d)(y - d) = d²",
            tags = "number_theory,diophantine,factoring",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_num_002"
        ),

        // COMBINATORICS & PROBABILITY
        ProblemEntity(
            id = "prob_comb_001",
            category = "Combinatorics",
            topicId = "comb_permutations",
            difficulty = "EASY",
            question = "In how many distinct ways can 5 books be arranged on a shelf if 2 specific books must always be next to each other?",
            optionA = "120",
            optionB = "48",
            optionC = "24",
            optionD = "60",
            correctAnswer = "B",
            solution = "Treat the 2 adjacent books as 1 block. There are now 4 items to arrange: 4! = 24 ways.\nThe 2 books within their block can be arranged in 2! = 2 ways.\nTotal arrangements = 24 × 2 = 48.",
            explanation = "Group restricted elements into a single composite entity.",
            formula = "Ways = (n - k + 1)! · k!",
            tags = "combinatorics,permutations",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_comb_001"
        ),
        ProblemEntity(
            id = "prob_prob_001",
            category = "Probability",
            topicId = "prob_classical",
            difficulty = "MEDIUM",
            question = "Two fair standard six-sided dice are rolled. What is the probability that the sum of the numbers is at least 9?",
            optionA = "5/18",
            optionB = "1/4",
            optionC = "7/36",
            optionD = "1/3",
            correctAnswer = "A",
            solution = "Total outcomes = 36.\nFavorable sums:\nSum 9: (3,6),(4,5),(5,4),(6,3) -> 4\nSum 10: (4,6),(5,5),(6,4) -> 3\nSum 11: (5,6),(6,5) -> 2\nSum 12: (6,6) -> 1\nTotal favorable = 4 + 3 + 2 + 1 = 10.\nProbability = 10/36 = 5/18.",
            explanation = "Count mutually exclusive outcomes totaling 9, 10, 11, and 12.",
            formula = "P(E) = n(E) / n(S)",
            tags = "probability,dice",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_prob_001"
        ),

        // CALCULUS
        ProblemEntity(
            id = "prob_calc_001",
            category = "Calculus",
            topicId = "calc_derivatives",
            difficulty = "MEDIUM",
            question = "Find the slope of the tangent line to f(x) = x³ - 3x² + 2x at x = 2.",
            optionA = "2",
            optionB = "0",
            optionC = "4",
            optionD = "-2",
            correctAnswer = "A",
            solution = "f'(x) = 3x² - 6x + 2.\nEvaluate at x = 2: f'(2) = 3(4) - 6(2) + 2 = 12 - 12 + 2 = 2.",
            explanation = "The slope of the tangent line is the derivative evaluated at the given point.",
            formula = "m = f'(x_0)",
            tags = "calculus,derivatives,tangent",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_calc_001"
        ),
        ProblemEntity(
            id = "prob_calc_002",
            category = "Calculus",
            topicId = "calc_integrals",
            difficulty = "HARD",
            question = "Evaluate the definite integral: ∫[0 to π] x · sin(x) dx",
            optionA = "π",
            optionB = "2π",
            optionC = "0",
            optionD = "1",
            correctAnswer = "A",
            solution = "Integration by parts: let u = x, dv = sin(x) dx => du = dx, v = -cos(x).\n∫ x sin(x) dx = -x cos(x) - ∫ (-cos(x)) dx = -x cos(x) + sin(x).\nEvaluating from 0 to π:\n[-π cos(π) + sin(π)] - [-0 + sin(0)] = [-π(-1) + 0] - [0] = π.",
            explanation = "Apply integration by parts formula: ∫ u dv = uv - ∫ v du.",
            formula = "∫ u dv = uv - ∫ v du",
            tags = "calculus,integration_by_parts",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_calc_002"
        ),

        // OLYMPIAD
        ProblemEntity(
            id = "prob_oly_001",
            category = "Olympiad",
            topicId = "olympiad_inequalities",
            difficulty = "EXPERT",
            question = "For positive real numbers a, b, c such that a + b + c = 3, what is the minimum possible value of: (a / (b + c)) + (b / (c + a)) + (c / (a + b))?",
            optionA = "1.5",
            optionB = "1.0",
            optionC = "2.0",
            optionD = "3.0",
            correctAnswer = "A",
            solution = "By Nesbitt's Inequality, for any positive real numbers a, b, c:\n(a / (b + c)) + (b / (c + a)) + (c / (a + b)) ≥ 3/2 = 1.5.\nEquality holds when a = b = c = 1.",
            explanation = "Nesbitt's inequality is a classic Olympiad theorem derived from AM-HM or Cauchy-Schwarz.",
            formula = "Σ (a / (b + c)) ≥ 3/2",
            tags = "olympiad,inequalities,nesbitt",
            generator = "MathMaster-CoreGen-v1",
            questionHash = "hash_oly_001"
        ),

        // MAXSUS ORIGINAL SAMPLE PROBLEMS
        ProblemEntity(
            id = "prob_maxsus_001",
            category = "MAXSUS",
            topicId = "maxsus_usmonov_1",
            difficulty = "HARD",
            question = "Maxsus Misol: If x + (1/x) = √5, evaluate the exact value of x⁵ + (1/x⁵).",
            optionA = "5√5",
            optionB = "11√5",
            optionC = "7√5",
            optionD = "25",
            correctAnswer = "A",
            solution = "x² + 1/x² = (√5)² - 2 = 3.\nx³ + 1/x³ = (x + 1/x)(x² + 1/x² - 1) = √5(3 - 1) = 2√5.\nMultiply: (x² + 1/x²)(x³ + 1/x³) = x⁵ + 1/x⁵ + x + 1/x.\n3 · 2√5 = (x⁵ + 1/x⁵) + √5 => 6√5 = (x⁵ + 1/x⁵) + √5 => x⁵ + 1/x⁵ = 5√5.",
            explanation = "Decompose higher powers using symmetric identities of order 2 and 3.",
            formula = "(x² + x⁻²)(x³ + x⁻³) = (x⁵ + x⁻⁵) + (x + x⁻¹)",
            tags = "maxsus,usmonov,symmetric_polynomials",
            generator = "MathMaster-MaxsusGen",
            questionHash = "hash_maxsus_001"
        ),
        ProblemEntity(
            id = "prob_maxsus_002",
            category = "MAXSUS",
            topicId = "maxsus_usmonov_2",
            difficulty = "EXPERT",
            question = "Maxsus Tenglama: Find the product of all real roots of: √(x² + 3) + √(x² - 1) = 4",
            optionA = "-1.75",
            optionB = "-3.25",
            optionC = "-2.25",
            optionD = "-1.00",
            correctAnswer = "B",
            solution = "Multiply by conjugate: (x² + 3) - (x² - 1) = 4.\nSo 4 / (√(x² + 3) - √(x² - 1)) = 4 => √(x² + 3) - √(x² - 1) = 1.\nAdd original equation: 2√(x² + 3) = 5 => √(x² + 3) = 2.5 => x² + 3 = 6.25 => x² = 3.25 = 13/4.\nRoots: x = ±√13 / 2. Their product is -(13/4) = -3.25.",
            explanation = "Multiply by the algebraic conjugate to eliminate radicals rapidly without extraneous quadratic squaring.",
            formula = "(√A - √B)(√A + √B) = A - B",
            tags = "maxsus,irrational_equations,conjugates",
            generator = "MathMaster-MaxsusGen",
            questionHash = "hash_maxsus_002"
        )
    )

    val specialContent = listOf(
        SpecialContentEntity(
            id = "spec_001",
            topicId = "maxsus_usmonov_1",
            title = "Usmonov To'plami: Algebraik Ayniyatlar Nazariyasi",
            subtitle = "Sintez va simmetrik ko'phadlar usullari",
            contentBody = "Ushbu bo'limda akademik litseylar va olimpiadalar darajasidagi murakkab algebraik ifodalarni soddalashtirish, Nyuton formulalari hamda simmetrik ko'phadlar yordamida yuqori darajali tenglamalarni yechish ko'rib chiqiladi.\n\nAsosiy metodologiya:\n1. O'zgaruvchilarni almashtirish (t = x + 1/x)\n2. Yig'indilar va ko'paytmalar simmetriyasi\n3. Qoldiqlar va ko'paytuvchilarga ajratishning nostandart usullari.",
            formulaSheet = "xⁿ + yⁿ = (x + y)(xⁿ⁻¹ - ...) \n(a + b + c)² = a² + b² + c² + 2(ab + bc + ca)",
            exampleCount = 5,
            orderIndex = 1
        ),
        SpecialContentEntity(
            id = "spec_002",
            topicId = "maxsus_usmonov_2",
            title = "Irratsional Tenglamalar: Qo'shma Ifoda Metodi",
            subtitle = "Kvadratga ko'tarmasdan ildizlardan qutilish usullari",
            contentBody = "Murakkab irratsional tenglamalarni yechishda to'g'ridan-to'g'ri kvadratga ko'tarish begona ildizlar va 4-darajali murakkab tenglamalarni keltirib chiqaradi. Qo'shma ifodaga ko'paytirish usuli esa tenglamani darhol chiziqli sistemaga aylantiradi.",
            formulaSheet = "(√A - √B)(√A + √B) = A - B \n√(f(x)) = g(x) <=> g(x) ≥ 0, f(x) = g²(x)",
            exampleCount = 4,
            orderIndex = 2
        ),
        SpecialContentEntity(
            id = "spec_003",
            topicId = "maxsus_usmonov_3",
            title = "Planimetriya: Chiziqli Geometriyadagi Kuchli Teoremalar",
            subtitle = "Cheva, Menelay va Aylana bo'yicha nuqta darajasi",
            contentBody = "Geometriyaning ushbu maxsus qismida Evklid geometriyasining klassik va chuqurlashtirilgan teoremalari jamlangan. Masalalarni yechishda nuqta darajasi (Power of a Point) va burchak bissektrisasi xossalaridan unumli foydalanish ko'nikmalari rivojlantiriladi.",
            formulaSheet = "PA · PB = PC · PD \nPT² = PA · PB",
            exampleCount = 6,
            orderIndex = 3
        )
    )
}
