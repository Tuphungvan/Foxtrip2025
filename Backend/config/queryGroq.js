require("dotenv").config();

async function queryGroq(model, prompt) {
    const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${process.env.GROQ_API_KEY}`
        },
        body: JSON.stringify({
            model: model || "llama-3.1-8b-instant",
            messages: [{ role: "user", content: prompt }],
            temperature: 0.7
        })
    });

    const data = await response.json();

    if (data.error) {
        throw new Error(data.error.message);
    }

    return data.choices[0].message.content.trim();
}

module.exports = { queryGroq };
