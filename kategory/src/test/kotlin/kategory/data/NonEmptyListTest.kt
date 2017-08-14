package kategory

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class NonEmptyListTest : UnitSpec() {
    init {

        testLaws(MonadLaws.laws(NonEmptyList, Eq.any()))
        testLaws(SemigroupKLaws.laws(
                NonEmptyList.semigroupK(),
                NonEmptyList.applicative(),
                Eq.any()))

        "map should modify values" {
            NonEmptyList.of(14).map { it * 3 } shouldBe NonEmptyList.of(42)
        }

        "flatMap should modify entity" {
            Option.Some(1).flatMap { Option.None } shouldBe Option.None
            Option.Some(1).flatMap { Option.Some("something") } shouldBe Option.Some("something")
            Option.None.flatMap { Option.Some("something") } shouldBe Option.None
        }

        "flatMap should work" {
            val nel = NonEmptyList.of(1, 2)
            val nel2 = nel.flatMap { it -> NonEmptyList.of(it, it) }
            nel2 shouldBe NonEmptyList.of(1, 1, 2, 2)
        }

        "NonEmptyListMonad.flatMap should be consistent with NonEmptyList#flatMap" {
            val nel = NonEmptyList.of(1, 2)
            val nel2 = NonEmptyList.of(1, 2)
            nel.flatMap { nel2 } shouldBe NonEmptyList.flatMap(nel) { nel2 }
        }

        "NonEmptyListMonad.binding should for comprehend over NonEmptyList" {
            val result = NonEmptyList.binding {
                val x = NonEmptyList.of(1).bind()
                val y = bind { NonEmptyList.of(2) }
                yields(x + y)
            }
            result shouldBe NonEmptyList.of(3)
        }

        "NonEmptyListMonad.binding should for comprehend over complex NonEmptyList" {
            val result = NonEmptyList.binding {
                val x = NonEmptyList.of(1, 2).bind()
                val y = bind { NonEmptyList.of(3) }
                yields(x + y)
            }
            result shouldBe NonEmptyList.of(4, 5)
        }

        "NonEmptyListMonad.binding should for comprehend over all values of multiple NonEmptyList" {
            forAll { a: Int, b: List<Int> ->
                val nel: NonEmptyList<Int> = NonEmptyList(a, b)
                val nel2 = NonEmptyList.of(1, 2)
                val result: HK<NonEmptyListHK, Int> = NonEmptyList.binding {
                    val x = bind { nel }
                    val y = nel2.bind()
                    yields(x + y)
                }
                result.ev().size == nel.size * nel2.size
            }
        }

        "NonEmptyListComonad.cobinding should for comprehend over NonEmptyList" {
            val result = NonEmptyList.cobinding {
                val x = !NonEmptyList.of(1)
                val y = NonEmptyList.of(2).extract()
                val z = extract { NonEmptyList.of(3) }
                x + y + z
            }
            result shouldBe 6
        }

        "NonEmptyListComonad.cobinding should for comprehend over complex NonEmptyList" {
            val result = NonEmptyList.cobinding {
                val x = !NonEmptyList.of(1, 2)
                val y = NonEmptyList.of(3).extract()
                val z = extract { NonEmptyList.of(4) }
                x + y + z
            }
            result shouldBe 8
        }

        "NonEmptyListComonad.cobinding should for comprehend over all values of multiple NonEmptyList" {
            forAll { a: Int, b: List<Int> ->
                val nel: NonEmptyList<Int> = NonEmptyList(a, b)
                val nel2 = NonEmptyList.of(1, 2)
                val nel3 = NonEmptyList.of(3, 4, 5)
                val result: Int = NonEmptyList.cobinding {
                    val x = !nel
                    val y = nel2.extract()
                    val z = extract { nel3 }
                    x + y + z
                }
                result == 1 + 3 + a
            }
        }

        "NonEmptyListComonad.duplicate should create an instance of NonEmptyList<NonEmptyList<A>>" {
            NonEmptyList.duplicate(NonEmptyList.of(3)) shouldBe NonEmptyList.of(NonEmptyList.of(3))
        }
    }
}
