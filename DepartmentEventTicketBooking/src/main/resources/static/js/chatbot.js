document.addEventListener('DOMContentLoaded', function() {
    initChatbot();
});

function initChatbot() {
    const widget = document.getElementById('chatbot-widget');
    if (!widget) {
        return;
    }

    const toggleButton = widget.querySelector('#chatbot-toggle');
    const closeButton = widget.querySelector('#chatbot-close');
    const chatWindow = widget.querySelector('#chatbot-window');
    const chatForm = widget.querySelector('#chatbot-form');
    const chatInput = widget.querySelector('#chatbot-input');
    const messageContainer = widget.querySelector('#chatbot-messages');

    toggleButton.addEventListener('click', function() {
        chatWindow.classList.toggle('open');
    });

    closeButton.addEventListener('click', function() {
        chatWindow.classList.remove('open');
    });

    chatForm.addEventListener('submit', function(event) {
        event.preventDefault();
        sendChatMessage(chatInput, messageContainer);
    });

    chatInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendChatMessage(chatInput, messageContainer);
        }
    });
}

function sendChatMessage(chatInput, messageContainer) {
    const text = chatInput.value.trim();
    if (!text) {
        return;
    }

    appendChatMessage(messageContainer, text, 'user');
    chatInput.value = '';

    fetch('/chatbot/message', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: text })
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(function(body) {
        appendChatMessage(messageContainer, body.response || 'Sorry, something went wrong.', 'bot');
    })
    .catch(function() {
        appendChatMessage(messageContainer, 'Sorry, I could not connect to the assistant right now.', 'bot');
    });
}

function appendChatMessage(container, text, sender) {
    const messageItem = document.createElement('div');
    messageItem.className = 'chatbot-message ' + sender;
    messageItem.textContent = text;
    container.appendChild(messageItem);
    container.scrollTop = container.scrollHeight;
}
