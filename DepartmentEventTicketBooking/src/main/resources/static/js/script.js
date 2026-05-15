/**
 * Frontend Validation & Interactive Features for Ticket Booking
 */
document.addEventListener('DOMContentLoaded', function() {
    initScrollAnimations();
    initInteractiveElements();
    initPowerToggle();

    const bookingForm = document.getElementById('bookingForm');
    if (bookingForm) {
        handleBookingForm(bookingForm);
    }

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

function initScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-fade');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    document.querySelectorAll('.card, .form-card, .alert, .data-table-container').forEach(el => {
        if (!el.classList.contains('animate-fade')) {
            observer.observe(el);
        }
    });
}

function initInteractiveElements() {
    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px)';
        });
        btn.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    document.querySelectorAll('.card').forEach(card => {
        card.addEventListener('mousedown', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');

            this.appendChild(ripple);
            setTimeout(() => ripple.remove(), 600);
        });
    });

    const counterElements = document.querySelectorAll('[data-counter]');
    counterElements.forEach(el => {
        animateCounter(el);
    });
}

function animateCounter(element) {
    const target = parseInt(element.getAttribute('data-counter')) || 0;
    const duration = 1500;
    const startTime = performance.now();

    const animate = (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const value = Math.floor(progress * target);
        element.textContent = value;

        if (progress < 1) {
            requestAnimationFrame(animate);
        }
    };

    requestAnimationFrame(animate);
}

function handleBookingForm(bookingForm) {
    bookingForm.addEventListener('submit', function(event) {
        let isValid = true;
        const errorMessages = [];

        const userName = document.getElementById('userName')?.value.trim();
        const email = document.getElementById('email')?.value.trim();
        const department = document.getElementById('department')?.value;
        const tickets = parseInt(document.getElementById('numberOfTickets')?.value);
        const availableTickets = parseInt(document.getElementById('availableTickets')?.value);

        if (!userName || !email || !department || isNaN(tickets)) {
            isValid = false;
            errorMessages.push('All fields are mandatory.');
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email && !emailRegex.test(email)) {
            isValid = false;
            errorMessages.push('Please enter a valid email address.');
        }

        if (tickets <= 0) {
            isValid = false;
            errorMessages.push('Number of tickets must be at least 1.');
        }

        if (!isNaN(availableTickets) && tickets > availableTickets) {
            isValid = false;
            errorMessages.push(`Cannot book ${tickets} tickets. Only ${availableTickets} available.`);
        }

        if (!isValid) {
            event.preventDefault();
            showErrors(errorMessages, bookingForm);
        }
    });

    const ticketInput = document.getElementById('numberOfTickets');
    const priceElement = document.getElementById('totalPriceDisplay');
    const ticketPrice = parseFloat(document.getElementById('ticketPriceValue')?.value || 0);

    if (ticketInput && priceElement) {
        ticketInput.addEventListener('input', function() {
            const qty = parseInt(this.value) || 0;
            const total = (qty * ticketPrice).toFixed(2);

            priceElement.style.opacity = '0.7';
            setTimeout(() => {
                priceElement.textContent = ticketPrice === 0 ? 'FREE' : '₹' + total;
                priceElement.style.opacity = '1';
            }, 100);
        });
    }
}

function showErrors(messages, bookingForm) {
    let errorContainer = document.getElementById('errorContainer');
    if (!errorContainer) {
        errorContainer = document.createElement('div');
        errorContainer.id = 'errorContainer';
        errorContainer.className = 'alert alert-danger';
        bookingForm.prepend(errorContainer);
    }

    errorContainer.innerHTML = messages.join('<br>');
    errorContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    errorContainer.style.animation = 'shake 0.5s';
    setTimeout(() => {
        errorContainer.style.animation = '';
    }, 500);
}

function resetForm() {
    const form = document.getElementById('bookingForm');
    if (form) {
        form.reset();
        const errorContainer = document.getElementById('errorContainer');
        if (errorContainer) {
            errorContainer.style.animation = 'fadeOut 0.3s';
            setTimeout(() => errorContainer.remove(), 300);
        }
        const priceElement = document.getElementById('totalPriceDisplay');
        const ticketPrice = parseFloat(document.getElementById('ticketPriceValue')?.value || 0);
        if (priceElement) {
            priceElement.textContent = ticketPrice === 0 ? 'FREE' : '₹' + ticketPrice.toFixed(2);
        }
    }
}

function initPowerToggle() {
    const powerToggle = document.getElementById('power-toggle');
    if (!powerToggle) {
        const toggleDiv = document.createElement('div');
        toggleDiv.id = 'power-toggle';
        toggleDiv.innerHTML = `
            <div class="power-lamp" id="powerLamp">
                <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C13.1 2 14 2.9 14 4V5H16V7H13V19H16V21H8V19H11V7H8V5H10V4C10 2.9 10.9 2 12 2M12 4V5H12V4Z"/>
                </svg>
            </div>
        `;
        document.body.appendChild(toggleDiv);

        const powerLamp = document.getElementById('powerLamp');
        powerLamp.addEventListener('click', function() {
            toggleSystemPower();
        });
    }
}

function toggleSystemPower() {
    const body = document.body;
    const powerLamp = document.getElementById('powerLamp');

    if (!powerLamp) {
        return;
    }

    if (body.classList.contains('system-offline')) {
        body.classList.remove('system-offline');
        powerLamp.classList.remove('offline');
        showBootSequence();
    } else {
        body.classList.add('system-offline');
        powerLamp.classList.add('offline');
        showShutdownSequence();
    }
}

function showBootSequence() {
    const bootScreen = document.createElement('div');
    bootScreen.className = 'system-boot';
    bootScreen.innerHTML = `
        <div class="boot-screen">
            <h1>INITIALIZING SECURE SYSTEMS</h1>
            <div class="boot-progress">
                <div class="boot-progress-bar"></div>
            </div>
        </div>
    `;
    document.body.appendChild(bootScreen);

    setTimeout(() => {
        bootScreen.classList.add('hidden');
        setTimeout(() => bootScreen.remove(), 1000);
    }, 4000);
}

function showShutdownSequence() {
    const shutdownScreen = document.createElement('div');
    shutdownScreen.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: var(--background);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
        color: var(--primary);
        font-family: 'JetBrains Mono', monospace;
        text-align: center;
    `;
    shutdownScreen.innerHTML = '<h1>SYSTEM SHUTDOWN<br>ACCESS TERMINATED</h1>';
    document.body.appendChild(shutdownScreen);

    setTimeout(() => {
        shutdownScreen.remove();
    }, 2000);
}

function handleLogin(event) {
    event.preventDefault();

    const form = event.target;
    const email = form.querySelector('input[type="text"]')?.value;
    const password = form.querySelector('input[type="password"]')?.value;

    if (!email || !password) {
        showLoginError('ACCESS DENIED: Credentials required');
        return;
    }



    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'AUTHENTICATING...';
    submitBtn.disabled = true;

    setTimeout(() => {
        showLoginSuccess('AUTHENTICATING...');
        setTimeout(() => {
            form.submit();
        }, 500);
    }, 500);
}

function showLoginError(message) {
    let errorDiv = document.querySelector('.login-error');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'login-error alert alert-danger';
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.prepend(errorDiv);
        }
    }
    errorDiv.textContent = message;
    errorDiv.style.animation = 'shake 0.5s';
    setTimeout(() => {
        errorDiv.style.animation = '';
    }, 500);
}

function showLoginSuccess(message) {
    let successDiv = document.querySelector('.login-success');
    if (!successDiv) {
        successDiv = document.createElement('div');
        successDiv.className = 'login-success alert alert-success';
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.prepend(successDiv);
        }
    }
    successDiv.textContent = message;
}

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });
});
