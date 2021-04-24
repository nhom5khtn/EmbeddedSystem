package csv.kotlin.test

data class Student(
        val studentId: String,
        val firstName: String,
        val lastName: String,
        val score: String
    ){
    override fun toString(): String {
        return "id: $studentId, $firstName $lastName, score: $score"
    }
}
