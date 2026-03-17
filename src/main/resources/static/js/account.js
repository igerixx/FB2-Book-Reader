const history = document.getElementById("history");
const logOutBtn = document.getElementById("log-out");

document.addEventListener("DOMContentLoaded", () => {
    fetch("http://localhost:8081/api/currentUser", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.user == null) {
            window.location.href = "http://localhost:8081/";
        }
        document.getElementById("username").textContent = data.user;
    })
    .catch(() => {
        window.location.href = "http://localhost:8081/";
    });
});

history.addEventListener("click", () => {
    // fetch("http://localhost:8081/history", {
    //     method: "POST",
    //     headers: {
    //         "Authorization": "Bearer " + localStorage.getItem("token")
    //     }
    // });
    window.location.href = "http://localhost:8081/history";
});

logOutBtn.addEventListener("click", () => {
    fetch("http://localhost:8081/api/logout", {
        method: "POST"
    });
    localStorage.removeItem("token");
    window.location.href = "http://localhost:8081/";
});