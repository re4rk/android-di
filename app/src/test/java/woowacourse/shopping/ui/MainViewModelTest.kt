package woowacourse.shopping.ui

import com.google.common.truth.Truth.assertThat
import kotlin.reflect.full.declaredMemberProperties
import org.junit.Test

class MainViewModelTest {
    @Test
    fun `리포지터리 프로퍼티는 2개이며 타입은 모두 인터페이스다`() {
        // given
        val repositories = MainViewModel::class.declaredMemberProperties
            .filter { it.name.endsWith("Repository") }

        // then
        assertThat(repositories.size).isEqualTo(2)

        repositories.forEach { property ->
            val type = Class.forName(property.returnType.toString())
            assertThat(type.isInterface).isTrue()
        }
    }
}
