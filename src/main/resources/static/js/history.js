let username;

document.addEventListener("DOMContentLoaded", async () => {
    const res = await fetch("http://localhost:8081/api/currentUser", {
        method: "POST"
    });

    const data = await res.json();
    username = data.user;

    if (username) {
        fetch("http://localhost:8081/api/books", {
            method: "POST",
            headers: {
            "Content-Type": "application/json"
            },
            body: username
        }).then(res => res.json())
        .then(data => {
            if (data.books.length > 0) {
                document.getElementById("empty").style.display = "none";
                const historyContainer = document.querySelector(".history-container");
                const historyLabel = document.querySelector(".history-label");
                for (book of data.books) {
                    const bookname = document.createElement("span");
                    const info = document.createElement("span");
                    const filename = document.createElement("span");
                    const time = document.createElement("span");
                    info.className = "info";
                    filename.className = "filename";

                    bookname.textContent = book.bookname;
                    filename.textContent = book.filename;
                    time.textContent = book.time.split("T")[0];

                    info.appendChild(filename);
                    info.appendChild(time);
                    
                    const div = document.createElement("div");
                    div.className = "book";
                    div.appendChild(bookname);
                    div.appendChild(info);

                    div.addEventListener("click", () => {
                        localStorage.setItem("file", filename.textContent);
                        window.location.href = "index.html";
                    });
                    
                    historyLabel.appendChild(div);
                }
                historyContainer.appendChild(historyLabel);
                document.body.appendChild(historyContainer);
            } else {
                document.getElementById("empty").style.display = "block";
            }
        })
    }
});