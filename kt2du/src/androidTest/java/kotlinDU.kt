
fun myPow(base: Double, n: Int) : Double {
    if (n == 0){
        return 1.00
    }

    return base * myPow(base, n - 1)
}

fun geometricSum(start: Double, ratio: Double, n: Int) : Double{
    return start * ((myPow(ratio, n) - 1) / (ratio - 1))
}

fun square(n: Int ){
    if (n % 2 == 0){
        for (i in 0 until n){
            for(j in 0 until n){
                if(i == 0 || i == (n - 1) || j == 0 || j == (n - 1)){
                    print("* ")
                } else {
                    print("  ")
                }
            }
            println()
        }
    } else {
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (i == (n / 2) || j == (n / 2)) {
                    print("* ")
                } else {
                    print("  ")
                }
            }
            println()
        }
    }
}

fun onlyChosen(firstArray: IntArray , secondArray: IntArray, decider : ( Int , Int ) -> Int) : IntArray {
    require(firstArray.size == secondArray.size)

    val result = IntArray(firstArray.size)
    for (i in firstArray.indices) {
        result[i] = decider(firstArray[i], secondArray[i])
    }
    return result
}

fun String.toggle(): String {
    var toggledString = ""

    for (c in this) {
        toggledString += when {
            c == ' ' -> '*'
            c.isLowerCase() -> c.uppercaseChar()
            c.isUpperCase() -> c.lowercaseChar()
            else -> c
        }
    }

    return toggledString
}

fun test(text: String): Int {
    var i = 0 //mutables vs non mutabes
    while(i < text.length && text[i] != 'a' ){ //Order matter
        i++
    }
    return i
}

interface HashTable<T> {
    val size : Int
    fun isEmpty(): Boolean = size == 0

    fun add(value: T): Boolean
    fun remove(value: T): Boolean
    operator fun contains(value: T): Boolean

    fun clear()
}

fun abs(x : Int) : Int {
    if (x < 0){
        return (x * -1)
    }
    return x
}

class MyHashTable<T>(
    private val capacity: Int = 16
) : HashTable<T> {   //Implementujeme interface od Hash Table

    private val buckets: Array<MutableList<T>> = Array(capacity) { mutableListOf<T>() } // Pole listů

    private var _size = 0 // _ -> interní proměná

    override val size: Int
        get() = _size

    override fun isEmpty(): Boolean {
        return _size == 0   // zavolá to interně a scčekne se to díky get
    }

    private fun index(value: T): Int {
        val hash = value?.hashCode() ?: 0   //null check, pokud je null vrať 0 -> ?: elvis, also build in hash fun
        return abs(hash) % capacity
    }

    override fun add(value: T): Boolean {
        val idx = index(value)
        val bucket = buckets[idx] // vrátí seznam z toho pole na daném indexu

        if (bucket.contains(value)) {
            return false
        }

        bucket.add(value)
        _size++
        return true
    }

    override fun remove(value: T): Boolean {
        val idx = index(value)
        val bucket = buckets[idx]

        val removed = bucket.remove(value) // můžeme udělat takto neboť remove je implementováno u mutablelist
        if (removed) {
            _size--
        }

        return removed
    }

    override operator fun contains(value: T): Boolean {
        val idx = index(value)
        return buckets[idx].contains(value) //stejně jak u removed
    }

    override fun clear() {
        for (bucket in buckets) { //stejně jak u removed, jenom to procházíme
            bucket.clear()
        }
        _size = 0
    }
}


fun main() {
    // --- myPow tests ---
    println("myPow tests")
    println("2^4 = ${myPow(2.0, 4)} (expected 16.0)")
    println("5^0 = ${myPow(5.0, 0)} (expected 1.0)")
    println()

    // --- geometricSum tests ---
    println("geometricSum tests")
    println("start=2 ratio=2 n=3 -> ${geometricSum(2.0, 2.0, 3)} (expected 14.0)")
    // 2 + 4 + 8 = 14
    println()

    // --- square tests ---
    println("square tests")
    println("square(4) should be hollow square:")
    square(4)
    println()
    println("square(5) should be cross:")
    square(5)
    println()

    // --- toggle tests ---
    println("toggle tests")
    println("\"petra\" -> \"${"petra".toggle()}\" (expected PETRA)")
    println("\"Ahoj Svete 123\" -> \"${"Ahoj Svete 123".toggle()}\" (expected aHOJ*sVETE*123)")
    println()

    // --- test(text) tests ---
    println("test(text) tests (index of first 'a' or length)")
    println("\"petra\" -> ${test("petra")} (expected 3)")
    println()

    // --- onlyChosen tests ---
    println("onlyChosen tests")
    val a = intArrayOf(1, 5, 3, 8)
    val b = intArrayOf(4, 2, 6, 7)

    val bigger = onlyChosen(a, b) { x, y -> if (x > y) x else y }
    println("Bigger: ${bigger.joinToString()} (expected 4, 5, 6, 8)")

    val smaller = onlyChosen(a, b) { x, y -> if (x < y) x else y }
    println("Smaller: ${smaller.joinToString()} (expected 1, 2, 3, 7)")

    val sum = onlyChosen(a, b) { x, y -> x + y }
    println("Sum: ${sum.joinToString()} (expected 5, 7, 9, 15)")
    println()

    // --- HashTable tests ---
    println("MyHashTable tests")
    val table = MyHashTable<Int>(capacity = 4)

    println("empty? ${table.isEmpty()} (expected true)") //string tamplating
    println("add 10 -> ${table.add(10)} (expected true)")
    println("add 10 again -> ${table.add(10)} (expected false)") // no duplicates
    println("add 14 -> ${table.add(14)} (expected true)")
    println("size -> ${table.size} (expected 2)")
    println("contains 10? ${10 in table} (expected true)")
    println("remove 10 -> ${table.remove(10)} (expected true)")
    println("contains 10? ${10 in table} (expected false)")
    println("remove 10 again -> ${table.remove(10)} (expected false)")
    println("size -> ${table.size} (expected 1)")
    table.clear()
    println("size after clear -> ${table.size} (expected 0)")
    println("empty after clear? ${table.isEmpty()} (expected true)")
}