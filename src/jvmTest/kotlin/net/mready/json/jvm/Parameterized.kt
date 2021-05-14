package net.mready.json.jvm

import net.mready.json.*
import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.experimental.ReferenceTests
import net.mready.json.jvm.adapters.JacksonJsonAdapter
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class ParameterizedJsonBuilderTests(override val adapter: JsonAdapter): JsonBuilderTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedJsonErrorTests(override val adapter: JsonAdapter): JsonErrorTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedJsonTypesTests(override val adapter: JsonAdapter): JsonTypesTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedMutationTests(override val adapter: JsonAdapter): MutationTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedParseTests(override val adapter: JsonAdapter): ParseTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedSerializeTests(override val adapter: JsonAdapter): SerializeTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}

@RunWith(Parameterized::class)
class ParameterizedReferenceTests(override val adapter: JsonAdapter): ReferenceTests() {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }
}