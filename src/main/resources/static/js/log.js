const submitSignInBtn = document.getElementById("submit-signin-btn");
const submitSignUpBtn = document.getElementById("submit-signup-btn");

submitSignUpBtn.addEventListener("click", () => {
    const username = document.getElementById("username-input").value;
    const password = document.getElementById("password-input").value;

    fetch("http://localhost:8081/api/signup", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    }).then(res => res.json())
    .then(data => {
        if (data.token[0] === "$") {
            document.getElementById("error-msg").textContent = data.token.substring(1, data.token.length);
            document.getElementById("error-msg").style.color = "red";
            document.getElementById("error-msg").style.display = "flex";
        } else {
            localStorage.setItem("token", data.token);
            document.getElementById("error-msg").textContent = "Succesfully created account and signed in";
            document.getElementById("error-msg").style.color = "green";
            document.getElementById("error-msg").style.display = "flex";
            
            setInterval(() => {
                document.getElementById("error-msg").style.display = "none";
                window.location.href = "index.html";
            }, 1000);
        }

    }).catch(() => {
        console.log("Error");
    });
})

submitSignInBtn.addEventListener("click", () => {
    const username = document.getElementById("username-input").value;
    const password = document.getElementById("password-input").value;

    fetch("http://localhost:8081/api/signin", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    }).then(res => res.json())
    .then(data => {
        localStorage.setItem("token", data.token);
        document.getElementById("error-msg").textContent = "Succesfully signed in";
        document.getElementById("error-msg").style.color = "green";
        document.getElementById("error-msg").style.display = "flex";
        
        setInterval(() => {
            document.getElementById("error-msg").style.display = "none";
            window.location.href = "index.html";
        }, 1000);
    }).catch(() => {
        document.getElementById("error-msg").textContent = "Username or password is incorrect!";
        document.getElementById("error-msg").style.color = "red";
        document.getElementById("error-msg").style.display = "flex";
    });
})