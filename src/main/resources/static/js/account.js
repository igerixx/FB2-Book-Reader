const history = document.getElementById("history");
const logOutBtn = document.getElementById("log-out");

document.addEventListener("DOMContentLoaded", () => {
    fetch("http://localhost:8081/api/currentUser", {
        method: "POST"
    })
    .then(res => res.json())
    .then(data => {
        document.getElementById("username").textContent = data.user;
    });
});

history.addEventListener("click", () => {
    window.location.href = "historyWindow.html";
});

logOutBtn.addEventListener("click", () => {
    fetch("http://localhost:8081/api/logout", {
        method: "POST"
    });
    window.location.href = "index.html";
});