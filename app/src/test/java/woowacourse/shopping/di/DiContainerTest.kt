package woowacourse.shopping.di

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DiContainerTest {
    interface FakeDiRepository {
        fun get(): String
    }

    interface FakeDiDataSource {
        fun get(): String
    }

    class FakeViewModel @DiInject constructor(
        private val diRepository: FakeDiRepository,
    ) {
        fun get(): String {
            return diRepository.get()
        }
    }

    class FakeDiProtoTypeRepository @DiInject constructor(
        private val diDataSource: FakeDiDataSource,
    ) : FakeDiRepository {
        override fun get(): String {
            return diDataSource.get()
        }
    }

    class FakeDiProtoTypeDataSource : FakeDiDataSource {
        override fun get(): String {
            return "FakeDiProtoTypeDataSource"
        }
    }

    private class FakeDiContainer : DiContainer() {
        private fun provideFakeDiDataSource(): FakeDiDataSource =
            this.createInstance(FakeDiProtoTypeDataSource::class)

        private fun provideFakeDiRepository(): FakeDiRepository =
            this.createInstance(FakeDiProtoTypeRepository::class)
    }

    private val fakeDiContainer = FakeDiContainer()

    @Test
    fun `DiContainer안에 있는 객체를 반환한다 1`() {
        // given & when
        val fakeDiRepository = fakeDiContainer.get(FakeDiRepository::class)

        // then
        assertTrue(fakeDiRepository is FakeDiProtoTypeRepository)
    }

    @Test
    fun `DiContainer안에 있는 객체를 반환한다 2`() {
        // given & when
        val fakeDiDataSource = fakeDiContainer.get(FakeDiDataSource::class)

        // then
        assertTrue(fakeDiDataSource is FakeDiProtoTypeDataSource)
    }

    @Test
    fun `첫번째 생성자 파라미터가 있으면 자동으로 주입하고 객체를 반환한다`() {
        // given & when
        val fakeDiRepository = fakeDiContainer.createInstance(FakeViewModel::class)

        // then
        assertTrue(fakeDiRepository is FakeViewModel)
    }

    @Test
    fun `의존성 부여 순서는 상관 없다`() {
        // given
        val fakeDiObject = object : DiContainer() {
            fun provideFakeDiRepository(): FakeDiRepository =
                this.createInstance(FakeDiProtoTypeRepository::class)

            fun provideFakeDiDataSource(): FakeDiDataSource =
                this.createInstance(FakeDiProtoTypeDataSource::class)
        }

        // when
        val fakeDiDataSource = fakeDiObject.get(FakeDiDataSource::class)

        // then
        assertTrue(fakeDiDataSource is FakeDiProtoTypeDataSource)
    }

    @Test
    fun `DiContainer에서 없는 리포지터리 객체를 요청하면 예외를 발생시킨다`() {
        // given
        class MockRepository

        // when
        runCatching { fakeDiContainer.get(MockRepository::class) }
            // then
            .onSuccess { assertEquals(it, null) }
            .onFailure { assertTrue(it is IllegalArgumentException) }
    }

    @Test
    fun `@DiInject가 있는 생성자를 찾아서 객체를 생성한다`() {
        // given
        class FakeDiInjectRepository @DiInject constructor(
            fakeDiInjectDataSource: FakeDiDataSource,
        ) : FakeDiRepository {
            override fun get(): String {
                return "FakeDiInjectRepository"
            }
        }

        // when
        val result = runCatching { fakeDiContainer.createInstance(FakeDiInjectRepository::class) }

        // then
        assertThat(result.isSuccess).isTrue

        // and
        assertThat(result.getOrThrow()).isInstanceOf(FakeDiInjectRepository::class.java)
    }

    @Test
    fun `@DiInject가 있는 생성자는 주 생성자가 아니어도 된다`() {
        // given
        class FakeViewModel constructor(fakeDiInjectRepository: FakeDiRepository) {
            @DiInject
            constructor(fakeDiInjectDataSource: FakeDiDataSource) :
                this(FakeDiProtoTypeRepository(fakeDiInjectDataSource))

            fun get(): String {
                return "FakeDiInjectRepository"
            }
        }

        // when
        val result = runCatching { fakeDiContainer.createInstance(FakeViewModel::class) }

        // then
        assertThat(result.isSuccess).isTrue

        // and
        assertThat(result.getOrThrow()).isInstanceOf(FakeViewModel::class.java)
    }

    @Test
    fun `@DiInject가 있는 생성자가 없으면 기본 생성자 찾고 기본 옵션 파라미터 이외에 것이 있으면 에러가 발생된다`() {
        // given
        class FakeDiInjectRepository(fakeDiInjectDataSource: FakeDiDataSource) :
            FakeDiRepository {
            override fun get(): String = "FakeDiInjectRepository"
        }

        // when
        val result = runCatching { fakeDiContainer.createInstance(FakeDiInjectRepository::class) }

        // then
        assertThat(result.isFailure).isTrue
    }

    @Test
    fun `@DiInject가 있는 생성자가 없으면 기본 생성자를 찾는다`() {
        // given
        class FakeDiInjectRepository : FakeDiRepository {
            override fun get(): String = "FakeDiInjectRepository"
        }

        // when
        val result = runCatching { fakeDiContainer.createInstance(FakeDiInjectRepository::class) }

        // then
        assertThat(result.isSuccess).isTrue

        // and
        assertThat(result.getOrThrow()).isInstanceOf(FakeDiInjectRepository::class.java)
    }

    @Test
    fun `@DiInject가 있는 생성자가 두개 이상 이면 예외를 발생시킨다`() {
        // given
        class FakeViewModel @DiInject constructor(fakeDiInjectRepository: FakeDiRepository) {
            @DiInject
            constructor(fakeDiInjectDataSource: FakeDiDataSource) :
                this(FakeDiProtoTypeRepository(fakeDiInjectDataSource))

            fun get(): String {
                return "FakeDiInjectRepository"
            }
        }

        // when
        val result = runCatching { fakeDiContainer.createInstance(FakeViewModel::class) }

        // then
        assertThat(result.isFailure).isTrue
    }
}
