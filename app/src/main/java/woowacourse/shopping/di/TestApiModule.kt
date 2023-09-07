package woowacourse.shopping.di

import com.re4rk.arkdi.Provides
import com.re4rk.arkdi.Singleton
import woowacourse.shopping.data.ProductSampleRepository
import woowacourse.shopping.repository.ProductRepository

// TODO : 머지전에 삭제 해야함
object TestApiModule : DiContainer() {

    @Provides
    fun provideProductRepository1(): ProductRepository =
        this.createInstance(ProductSampleRepository::class)

    @Singleton
    @Provides
    fun provideProductRepository2(): ProductRepository =
        this.createInstance(ProductSampleRepository::class)

    @Provides
    fun provideProductRepository(fakeDataSource: FakeDataSource): FakeRepository =
        FakeDefaultRepository(fakeDataSource)

    @Provides
    fun provideFakeDataSource(): FakeDataSource =
        FakeDefaultDataSource()
}

interface FakeDataSource {
    fun get(): String
}

class FakeDefaultDataSource : FakeDataSource {
    override fun get(): String {
        return "default"
    }
}

interface FakeRepository {
    fun get(): String
}

class FakeDefaultRepository(
    private val fakeDataSource: FakeDataSource
) : FakeRepository {
    override fun get(): String {
        return fakeDataSource.get()
    }
}
