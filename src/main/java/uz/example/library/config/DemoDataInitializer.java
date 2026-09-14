package uz.example.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uz.example.library.entity.Author;
import uz.example.library.entity.Book;
import uz.example.library.entity.Category;
import uz.example.library.entity.Reader;
import uz.example.library.enums.BookStatus;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.repository.AuthorRepository;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.CategoryRepository;
import uz.example.library.repository.ReaderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public DemoDataInitializer(
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository,
            BookRepository bookRepository,
            ReaderRepository readerRepository
    ) {
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }

    @Override
    public void run(String... args) {

        if (
                authorRepository.count() > 0
                        || categoryRepository.count() > 0
                        || bookRepository.count() > 0
                        || readerRepository.count() > 0
        ) {
            return;
        }

        List<Author> authors = createAuthors();
        List<Category> categories = createCategories();

        authorRepository.saveAll(authors);
        categoryRepository.saveAll(categories);

        List<Book> books =
                createBooks(
                        authors,
                        categories
                );

        bookRepository.saveAll(books);

        List<Reader> readers =
                createReaders();

        readerRepository.saveAll(readers);
    }

    private List<Author> createAuthors() {

        return List.of(
                new Author(
                        "George Orwell",
                        LocalDate.of(1903, 6, 25),
                        "United Kingdom",
                        "English novelist, essayist and journalist."
                ),
                new Author(
                        "Robert C. Martin",
                        LocalDate.of(1952, 12, 5),
                        "United States",
                        "Software engineer and author known for Clean Code."
                ),
                new Author(
                        "Joshua Bloch",
                        LocalDate.of(1961, 8, 28),
                        "United States",
                        "Software engineer and author of Effective Java."
                ),
                new Author(
                        "Erich Gamma",
                        LocalDate.of(1961, 3, 13),
                        "Switzerland",
                        "Software engineer and co-author of Design Patterns."
                ),
                new Author(
                        "Fyodor Dostoevsky",
                        LocalDate.of(1821, 11, 11),
                        "Russia",
                        "Russian novelist and philosopher."
                )
        );
    }

    private List<Category> createCategories() {

        return List.of(
                new Category(
                        "Programming",
                        "Dasturlash va software development kitoblari"
                ),
                new Category(
                        "Software Engineering",
                        "Software architecture va engineering kitoblari"
                ),
                new Category(
                        "Fiction",
                        "Badiiy adabiyot"
                ),
                new Category(
                        "Classic",
                        "Klassik adabiyot"
                ),
                new Category(
                        "Technology",
                        "Texnologiya va IT kitoblari"
                )
        );
    }

    private List<Book> createBooks(
            List<Author> authors,
            List<Category> categories
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        Author orwell = authors.get(0);
        Author martin = authors.get(1);
        Author bloch = authors.get(2);
        Author gamma = authors.get(3);
        Author dostoevsky = authors.get(4);

        Category programming = categories.get(0);
        Category softwareEngineering = categories.get(1);
        Category fiction = categories.get(2);
        Category classic = categories.get(3);
        Category technology = categories.get(4);

        return List.of(
                new Book(
                        "9780451524935",
                        "1984",
                        orwell,
                        fiction,
                        1949,
                        5,
                        5,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780451526342",
                        "Animal Farm",
                        orwell,
                        classic,
                        1945,
                        4,
                        4,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780132350884",
                        "Clean Code",
                        martin,
                        programming,
                        2008,
                        6,
                        6,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780134494166",
                        "Clean Architecture",
                        martin,
                        softwareEngineering,
                        2017,
                        5,
                        5,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780135974445",
                        "Agile Software Development",
                        martin,
                        softwareEngineering,
                        2002,
                        3,
                        3,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780134685991",
                        "Effective Java",
                        bloch,
                        programming,
                        2018,
                        7,
                        7,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780321356680",
                        "Java Puzzlers",
                        bloch,
                        programming,
                        2005,
                        3,
                        3,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780201633610",
                        "Design Patterns",
                        gamma,
                        softwareEngineering,
                        1994,
                        5,
                        5,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780596007126",
                        "Head First Design Patterns",
                        gamma,
                        programming,
                        2004,
                        4,
                        4,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780140449136",
                        "Crime and Punishment",
                        dostoevsky,
                        classic,
                        1866,
                        5,
                        5,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780374528379",
                        "The Brothers Karamazov",
                        dostoevsky,
                        classic,
                        1880,
                        4,
                        4,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780140449242",
                        "Notes from Underground",
                        dostoevsky,
                        fiction,
                        1864,
                        3,
                        3,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780137081073",
                        "The Clean Coder",
                        martin,
                        softwareEngineering,
                        2011,
                        4,
                        4,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780134757599",
                        "Refactoring",
                        martin,
                        technology,
                        2018,
                        5,
                        5,
                        BookStatus.ACTIVE,
                        now
                ),
                new Book(
                        "9780131177055",
                        "Working Effectively with Legacy Code",
                        martin,
                        technology,
                        2004,
                        3,
                        3,
                        BookStatus.ACTIVE,
                        now
                )
        );
    }

    private List<Reader> createReaders() {

        LocalDateTime now =
                LocalDateTime.now();

        return List.of(
                new Reader(
                        "Ali Valiyev",
                        "+998901111111",
                        "ali@example.com",
                        now,
                        ReaderStatus.ACTIVE
                ),
                new Reader(
                        "Vali Aliyev",
                        "+998902222222",
                        "vali@example.com",
                        now,
                        ReaderStatus.ACTIVE
                ),
                new Reader(
                        "Aziza Karimova",
                        "+998903333333",
                        "aziza@example.com",
                        now,
                        ReaderStatus.ACTIVE
                ),
                new Reader(
                        "Sardor Rasulov",
                        "+998904444444",
                        "sardor@example.com",
                        now,
                        ReaderStatus.ACTIVE
                ),
                new Reader(
                        "Madina Toshpulatova",
                        "+998905555555",
                        "madina@example.com",
                        now,
                        ReaderStatus.ACTIVE
                )
        );
    }
}