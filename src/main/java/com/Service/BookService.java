package com.Service;

import com.Entity.Book;
import com.Entity.Role;
import com.Entity.User;
import com.Repository.BookRepository;
import com.igerixx.Reader.XMLReader;
import com.igerixx.Reader.XMLReaderConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class BookService {
    private final UserService userService;
    private final AuthService authService;
    private final BookRepository bookRepository;

    public BookService(UserService userService, BookRepository bookRepository, AuthService authService) {
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.authService = authService;
    }

    public ResponseEntity<Map<String, ?>> upload(MultipartFile multipartFile, String filename) throws IOException {
        InputStream is = null;
        XMLReader reader = null;
        InputStream tempIs = null;

        String username = authService.currentUser().getName();

        final String filePath = !username.equals("anonymousUser")
                ? new File("Files/" + authService.currentUser().getName()).getAbsolutePath()
                : new File("Files/").getAbsolutePath();

        if (multipartFile != null) {
            is = multipartFile.getInputStream();
            tempIs = multipartFile.getInputStream();
        } else if (filename != null) {
            File file = new File(filePath + "\\" + filename);
            is = new FileInputStream(file);
            tempIs = new FileInputStream(file);
        } else return null;

        XMLReader tempReader = new XMLReader(tempIs);
        // Change encoding
        if (tempReader.hasNext()) {
            tempReader.next();
            tempReader.next();
            reader = new XMLReader(is, Charset.forName(tempReader.getAttributeValue("encoding")));
        }
        reader.setIgnoreComments(true);

        Map<String, String> imageMap = new HashMap<>();
        List<String> prphs = new ArrayList<>();
        String bookName = "";
        StringBuilder base64;
        boolean isMarkdown = false;

        int event;
        while (reader.hasNext()) {
            event = reader.next();

            if (event == XMLReaderConstants.START_ELEMENT) {

                // --- Book name ---
                if (reader.getLocalName().equals("description")) {
                    event = reader.next();
                    String tag = "";
                    while (EOFLoop(event)) {
                        event = reader.next();

                        if (event == XMLReaderConstants.START_ELEMENT
                                && (reader.getLocalName().equals("genre"))) {
                            continue;
                        }

                        if (event == XMLReaderConstants.START_ELEMENT
                                && reader.getLocalName().equals("coverpage")) {
                            reader.next();
                            prphs.add(reader.getAttributeValue("l:href"));
                            reader.next();
                            reader.next();
                            // <coverpage> - skip tag 1 time
                            //     <image id="" /> skip tag 2 times (self-close tag return START_ELEMENT and END_ELEMENT)
                            // </coverpage>
                            continue;
                        }

                        if (event == XMLReaderConstants.START_ELEMENT
                                && reader.getLocalName().equals("book-title")) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(String.format("<span class='%s'>", reader.getLocalName()));
                            reader.next();
                            stringBuilder.append(reader.getText()).append("</span><br>");
                            bookName = reader.getText();
                            prphs.add(stringBuilder.toString());
                        }

                        boolean isParagraph = reader.getLocalName().equals("author")
                                || reader.getLocalName().equals("book-title")
                                || reader.getLocalName().equals("annotation")
                                || reader.getLocalName().equals("date");

                        tag = isParagraph ? reader.getLocalName() : "";

                        if (event == XMLReaderConstants.START_ELEMENT
                                && !tag.isEmpty()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            while (EOFLoop(event)) {
                                if (event == XMLReaderConstants.END_ELEMENT
                                        && reader.getLocalName().equals(tag)) {
                                    stringBuilder.append("</span><br>");
                                    break;
                                }

                                if (event == XMLReaderConstants.START_ELEMENT
                                        && (reader.getLocalName().equals("home-page")
                                        || reader.getLocalName().equals("id"))) {
                                    reader.next();
                                    reader.next();
                                } else if (event == XMLReaderConstants.START_ELEMENT) {
                                    stringBuilder.append(String.format("<span class='%s'>", reader.getLocalName()));
                                }

                                if (event == XMLReaderConstants.CHARACTERS) {
                                    stringBuilder
                                            .append(!stringBuilder.isEmpty() ? "" : String.format("<span class='%s'>", tag))
                                            .append(reader.getText())
                                            .append(" </span>");
                                }

                                event = reader.next();
                            }

                            if (!stringBuilder.isEmpty()) {
                                prphs.add(stringBuilder.toString());
                            }
                        }


                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals("title-info")) {
                            break;
                        }
                    }

                    while (EOFLoop(event)) {
                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals("description")) {
                            break;
                        }
                        event = reader.next();
                    }

                    continue;
                }

                // --- Titles ---
                if (reader.getLocalName().equals("title")) {
                    event = reader.next();
                    while (EOFLoop(event)) {
                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals("title")) {
                            break;
                        }

                        if (!reader.getText().isEmpty()) {
                            prphs.add("<br><span class='title'>" + reader.getText() + "</span><br>");
                        }

                        event = reader.next();
                    }
                    continue;
                }

                // --- Subtitles ---
                if (reader.getLocalName().equals("subtitle")) {
                    Stack<String> tags = new Stack<>();
                    tags.push(reader.getLocalName());
                    String tag = reader.getLocalName();
                    event = reader.next();
                    StringBuilder stringBuilder = new StringBuilder();
                    while (EOFLoop(event)) {
                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals("subtitle")) {
                            break;
                        }

                        if (event == XMLReaderConstants.START_ELEMENT
                                && !reader.getLocalName().isEmpty()
                                && !reader.getLocalName().equals(tags.peek())) {
                            tags.push(reader.getLocalName());
                            stringBuilder
                                    .append("<br><span class='")
                                    .append(tag)
                                    .append("'>")
                                    .append(insideTag(reader, tags))
                                    .append("<br>");
                        } else if (event == XMLReaderConstants.START_ELEMENT) {
                            tags.push(reader.getLocalName());
                        }

                        event = reader.next();
                        if (!tag.isEmpty()
                                && tag.equals(tags.firstElement())
                                && tags.size() > 1) {
                            tags.pop();
                        }

                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals(tags.peek())) {
                            break;
                        }
                    }

                    if (!stringBuilder.isEmpty()) {
                        prphs.add(stringBuilder.toString());
                    }

                    continue;
                }

                // --- Base64 image ---
                if (reader.getLocalName().equals("binary")) {
                    String id = reader.getAttributeValue("id");
                    base64 = new StringBuilder();
                    base64
                            .append("data:")
                            .append(reader.getAttributeValue("content-type"))
                            .append(";base64,");


                    event = reader.next();
                    while (EOFLoop(event)) {
                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals("binary")) {
                            break;
                        }

                        if (!reader.getText().isEmpty()) {
                            base64.append(reader.getText());
                        }

                        event = reader.next();
                    }

                    imageMap.put(id, base64.toString());
                    continue;
                }

                // --- Image ---
                if (reader.getLocalName().equals("image")) {
                    prphs.add(reader.getAttributeValue("l:href"));
                    reader.next();
                    continue;
                }

                // --- Paragraph ---
                if (!reader.getLocalName().isEmpty()) {
                    if (reader.getLocalName().equals("id")
                            || reader.getLocalName().equals("title")
                            || reader.getLocalName().equals("version")
                            || reader.getLocalName().equals("subtitle")
                            || reader.getLocalName().equals("section")
                            || reader.getLocalName().equals("FictionBook")
                            || reader.getLocalName().equals("stylesheet")
                            || reader.getLocalName().equals("body")) {
                        continue;
                    }

                    if (reader.getLocalName().equals("empty-line")) {
                        prphs.add("<br>");
                        continue;
                    }

                    boolean isParagraph = reader.getLocalName().equals("p")
                            || reader.getLocalName().equals("cite")
                            || reader.getLocalName().equals("table")
                            || reader.getLocalName().equals("tr")
                            || reader.getLocalName().equals("td");

                    String tag = isParagraph ? reader.getLocalName() : "";

                    Stack<String> tags = new Stack<>();
                    tags.push(reader.getLocalName());
                    event = reader.next();
                    StringBuilder stringBuilder = new StringBuilder();
                    while (EOFLoop(event)) {
                        if (event == XMLReaderConstants.CHARACTERS
                                && !reader.getText().isEmpty()) {
                            String text = reader.getText();
                            if (reader.getText().startsWith("```") && !isMarkdown) {
                                reader.next();
                                reader.next();
                                reader.next();
                                isMarkdown = true;
                                text = reader.getText();
                            } else if (reader.getText().startsWith("```") && isMarkdown) {
                                isMarkdown = false;
                                if (reader.getText().trim().length() == 3) {
                                    reader.next();
                                    text = "";
                                } else {
                                    text = reader.getText().replace("```", "");
                                }
                                reader.next();
                            }
                            String tagName = isMarkdown ? "code" : tags.peek().equals("p") ? "span" : tags.peek();
                            stringBuilder
                                    .append("<span class='")
                                    .append(tagName)
                                    .append("'>")
                                    .append(text)
                                    .append(" </span>");
                        }

                        if (event == XMLReaderConstants.START_ELEMENT
                                && !reader.getLocalName().isEmpty()
                                && !reader.getLocalName().equals(tags.peek())) {
                            tags.push(reader.getLocalName());
                            String tagName = isMarkdown ? "code" : tags.peek().equals("p") ? "span" : tags.peek();
                            stringBuilder
                                    .append("<span class='")
                                    .append(tagName)
                                    .append("'>")
                                    .append(insideTag(reader, tags));
                        }
                        else if (event == XMLReaderConstants.START_ELEMENT) {
                            tags.push(reader.getLocalName());
                        }

                        event = reader.next();
                        if (!tag.isEmpty()
                                && tag.equals(tags.firstElement())
                                && tags.size() > 1) {
                            tags.pop();
                        }

                        if (event == XMLReaderConstants.END_ELEMENT
                                && reader.getLocalName().equals(tags.peek())) {
                            break;
                        }
                    }

                    if (!stringBuilder.isEmpty()) {
                        prphs.add(stringBuilder.toString());
                    }
                }
            }
        }

        for (int i = 0; i < prphs.size(); i++) {
            if (prphs.get(i).charAt(0) == '#') {
                String id = prphs.get(i).replace("#", "");
                prphs.set(i, String.format("<img alt=\"%s\" class=\"img\" src=\"%s\"/>", id, imageMap.get(id)));
            }
        }

        if (!authService.currentUser().getName().equals("anonymousUser") && multipartFile != null) {
            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdirs();

            multipartFile.transferTo(new File(dir, multipartFile.getOriginalFilename()));

            User user = userService.findByUsername(authService.currentUser().getName());

            Book book = new Book();
            book.setFilename(multipartFile.getOriginalFilename());
            book.setBookname(bookName);
            book.setTime(ZonedDateTime.now());
            book.setUser(user);

            if (!existBook(book.getFilename(), book.getUser().getId()))
                saveBook(book);
        }

        return ResponseEntity
                .ok()
                .body(Map.of(
                    "paragraphs", prphs,
                    "book", bookName
                ));
    }

    private String insideTag(XMLReader reader, Stack<String> tags) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int event = reader.next();
        String rootTag = tags.peek();
        while (event != XMLReaderConstants.END_ELEMENT
                && !reader.getLocalName().equals(rootTag)) {
            if (event == XMLReaderConstants.START_ELEMENT) {
                tags.push(reader.getLocalName());
                stringBuilder
                        .append("<span class='")
                        .append(tags.peek())
                        .append("'>");
            }

            if (event == XMLReaderConstants.CHARACTERS) {
                stringBuilder
                        .append(reader.getText())
                        .append(" </span>");
            }

            event = reader.next();
        }

        return stringBuilder.toString();
    }

    private boolean EOFLoop(int event) {
        return event != XMLReaderConstants.END_DOCUMENT;
    }

    public ResponseEntity<Map<String, ?>> books(String username) {
        Long id;
        try {
            id = userService.findByUsername(username).getId();
        } catch (Exception _) {
            return ResponseEntity
                    .ok()
                    .body(Map.of("books", ""));
        }
        List<Map<String, ?>> bookList = new ArrayList<>();
        for (var book : bookRepository.findAllByUserId(id)) {
            Map<String, ?> bk = Map.of("filename", book.getFilename(), "time", book.getTime(), "bookname", book.getBookname());
            bookList.add(bk);
        }
        return ResponseEntity
                .ok()
                .body(Map.of("books", bookList));
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    public boolean existBook(String filename, Long userId) {
        return bookRepository.existsByFilenameAndUserId(filename, userId);
    }
}
