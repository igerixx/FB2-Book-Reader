let paraphs = new Array();
let paragraphPage = 0;
let maxPage = 0;

const logBtn = document.getElementById("log-btn");
const input = document.getElementById("file-input");
const headTitle = document.getElementById("head-title");
const accCircle = document.getElementById("acc-circle");

document.addEventListener("DOMContentLoaded", () => {
    fetch("http://localhost:8081/api/currentUser", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.user === null) {
            window.location.href = "http://localhost:8081/";
        }

        document.getElementById("acc-circle").textContent = data.user.at(0).toUpperCase();
        document.getElementById("acc-circle").style.display = "flex";
        document.getElementById("acc-circle").style.backgroundColor = "rgb(" + Math.random() * 255 + ", " + Math.random() * 255 + ", " + Math.random() * 255 + ")";
    });

    if (localStorage.getItem("file")) {
        filename = localStorage.getItem("file");
        localStorage.removeItem("file");
        loadBook(filename);
    }

});

input.addEventListener("change", function () {
    if (this.files.length > 0) {
        loadBook(this.files[0]);
    }
});

logBtn.addEventListener("click", () => {
    window.location.href = "http://localhost:8081/login";
});

headTitle.addEventListener("click", () => {
    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
})

accCircle.addEventListener("click", () => {
    // fetch("http://localhost:8081/account", {
    //     method: "POST",
    //     headers: {
    //         "Authorization": "Bearer " + localStorage.getItem("token")
    //     }
    // });
    window.location.href = "http://localhost:8081/account";
});

function loadBook(filename) {
    const main = document.getElementById("main");
    main.replaceChildren();
    main.style.display = "none";
    let body;
    if (typeof filename === "string") {
        url = "/api/uploadByH";
        body = filename;
    } else {
        url = "/api/uploadByF";
        body = new FormData();
        body.append("data", filename);
    }
    
    fetch(url, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        },
        body: body
    })
    .then((response) => {
        return response.json();
    })
    .then((data) => {
        const paragraphs = data.paragraphs;
        const bookName = data.book;

        document.title = bookName;

        let index = 0;
        fragment = document.createDocumentFragment();
        for (const paragraph of paragraphs) {
            if (!paragraph.startsWith("<img")) {
                const p = document.createElement("p");
                p.innerHTML = "\t" + paragraph;
                p.className = "p";
                fragment.append(p);
            } else {
                const div = document.createElement("div");
                div.innerHTML = "<br>" + paragraph + "<br>";
                div.className = "div";
                fragment.append(div);   
            }
        }
        main.append(fragment);
        
        main.style.display = "block";
        document.body.append(main);
    });
}