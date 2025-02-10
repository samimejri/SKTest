package com.example.skissuetest

import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.KeyCredential
import com.microsoft.semantickernel.Kernel
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt
import com.microsoft.semantickernel.semanticfunctions.KernelPromptTemplateFactory
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService


class SKManager {
    private val CLIENT_KEY =
        "insert key here"
    private val MODEL_ID: String = "gpt-4o"

    private val prompt = "Give a persona profile that can be interested with this {{ \$product }}"

    val client =
        OpenAIClientBuilder()
            .credential(KeyCredential(CLIENT_KEY))
            .buildAsyncClient()

    val chat: ChatCompletionService = OpenAIChatCompletion.builder()
        .withModelId(MODEL_ID)
        .withOpenAIAsyncClient(client)
        .build()

    fun testPrompt(product: String): Persona? {
        val promptTemplate = KernelPromptTemplateFactory()
            .tryCreate(
                PromptTemplateConfig
                    .builder()
                    .withTemplate(
                        prompt
                    )
                    .withTemplateFormat("semantic-kernel")
                    .build()
            )

        val kernel = Kernel.builder()
            .withAIService(ChatCompletionService::class.java, chat)
            .build()

        var renderedPrompt = promptTemplate.renderAsync(
            kernel,
            KernelFunctionArguments.builder()
                .withVariable("product", product)
                .build(),
            null)
            .block()

        val args = KernelFunctionArguments.builder().withVariable("product", product).build()
        KernelFunctionFromPrompt.builder<Persona>().withTemplate(prompt).build()

        return kernel.invokePromptAsync<Persona>(prompt, args).block().result
    }
}