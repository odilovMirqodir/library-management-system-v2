package uz.example.library.service;

import org.springframework.stereotype.Service;
import uz.example.library.dto.request.ReaderCreateRequest;
import uz.example.library.dto.request.ReaderUpdateRequest;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.dto.response.ReaderResponse;
import uz.example.library.entity.Reader;
import uz.example.library.enums.LoanStatus;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.exception.DuplicateEntityException;
import uz.example.library.exception.EntityNotFoundException;
import uz.example.library.exception.ValidationException;
import uz.example.library.mapper.ReaderMapper;
import uz.example.library.repository.LoanRepository;
import uz.example.library.repository.ReaderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;
    private final ReaderMapper readerMapper;
    private final LoanRepository loanRepository;

    public ReaderService(
            ReaderRepository readerRepository,
            ReaderMapper readerMapper,
            LoanRepository loanRepository
    ) {
        this.readerRepository = readerRepository;
        this.readerMapper = readerMapper;
        this.loanRepository = loanRepository;
    }

    public ReaderResponse create(
            ReaderCreateRequest request
    ) {

        String fullName =
                request.getFullName().trim();

        String phone =
                request.getPhone().trim();

        if (readerRepository.existsByPhone(phone)) {

            throw new DuplicateEntityException(
                    "Bunday telefon raqamli kitobxon allaqachon mavjud"
            );
        }

        Reader reader =
                readerMapper.toEntity(request);

        reader.setFullName(fullName);
        reader.setPhone(phone);

        if (reader.getEmail() != null) {
            reader.setEmail(
                    reader.getEmail().trim()
            );
        }

        reader.setRegisteredAt(
                LocalDateTime.now()
        );

        reader.setStatus(
                ReaderStatus.ACTIVE
        );

        Reader savedReader =
                readerRepository.save(reader);

        return readerMapper.toResponse(
                savedReader
        );
    }

    public ReaderResponse findById(
            Long id
    ) {

        Reader reader =
                readerRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitobxon topilmadi"
                                )
                        );

        return readerMapper.toResponse(reader);
    }

    public PageResponse<ReaderResponse> findAll(
            String search,
            ReaderStatus status,
            String sort,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new ValidationException(
                    "Page 0 dan kichik bo'lishi mumkin emas"
            );
        }

        if (size < 1 || size > 100) {
            throw new ValidationException(
                    "Size 1 dan 100 gacha bo'lishi kerak"
            );
        }

        List<Reader> readers;

        if (
                status != null
                        && search != null
                        && !search.isBlank()
        ) {

            String value =
                    search.trim();

            readers = new ArrayList<>(
                    readerRepository
                            .findByStatusAndFullNameContainingIgnoreCaseOrStatusAndPhoneContaining(
                                    status,
                                    value,
                                    status,
                                    value
                            )
            );

        } else if (status != null) {

            readers = new ArrayList<>(
                    readerRepository.findByStatus(
                            status
                    )
            );

        } else if (
                search != null
                        && !search.isBlank()
        ) {

            String value =
                    search.trim();

            readers = new ArrayList<>(
                    readerRepository
                            .findByFullNameContainingIgnoreCaseOrPhoneContaining(
                                    value,
                                    value
                            )
            );

        } else {

            readers = new ArrayList<>(
                    readerRepository.findAll()
            );
        }

        applySort(
                readers,
                sort
        );

        long totalElements =
                readers.size();

        long start =
                (long) page * size;

        List<Reader> pageReaders;

        if (start >= readers.size()) {

            pageReaders =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            readers.size()
                    );

            pageReaders =
                    readers.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<ReaderResponse> responses =
                new ArrayList<>();

        for (Reader reader : pageReaders) {

            responses.add(
                    readerMapper.toResponse(reader)
            );
        }

        int totalPages =
                (int) Math.ceil(
                        (double) totalElements / size
                );

        return new PageResponse<>(
                responses,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private void applySort(
            List<Reader> readers,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("name")
                        || sort.equalsIgnoreCase("fullName")
                        || sort.equalsIgnoreCase("nameAsc")
        ) {

            readers.sort(
                    Comparator.comparing(
                            Reader::getFullName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("nameDesc")
                        || sort.equalsIgnoreCase("fullNameDesc")
        ) {

            readers.sort(
                    Comparator.comparing(
                                    Reader::getFullName,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .reversed()
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("registeredAt")
                        || sort.equalsIgnoreCase("registeredAtDesc")
        ) {

            readers.sort(
                    Comparator.comparing(
                                    Reader::getRegisteredAt,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            )
                            .reversed()
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("registeredAtAsc")
        ) {

            readers.sort(
                    Comparator.comparing(
                            Reader::getRegisteredAt,
                            Comparator.nullsLast(
                                    Comparator.naturalOrder()
                            )
                    )
            );

            return;
        }

        throw new ValidationException(
                "Sort qiymati noto'g'ri. name, nameDesc, registeredAt yoki registeredAtAsc ishlating"
        );
    }

    public ReaderResponse update(
            Long id,
            ReaderUpdateRequest request
    ) {

        Reader reader =
                readerRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitobxon topilmadi"
                                )
                        );

        String fullName =
                request.getFullName().trim();

        String phone =
                request.getPhone().trim();

        if (
                readerRepository
                        .existsByPhoneAndIdNot(
                                phone,
                                id
                        )
        ) {

            throw new DuplicateEntityException(
                    "Bunday telefon raqamli kitobxon allaqachon mavjud"
            );
        }

        readerMapper.updateEntity(
                reader,
                request
        );

        reader.setFullName(fullName);
        reader.setPhone(phone);

        if (reader.getEmail() != null) {
            reader.setEmail(
                    reader.getEmail().trim()
            );
        }

        Reader updatedReader =
                readerRepository.save(reader);

        return readerMapper.toResponse(
                updatedReader
        );
    }

    public ReaderResponse changeStatus(
            Long id,
            ReaderStatus status
    ) {

        Reader reader =
                readerRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitobxon topilmadi"
                                )
                        );

        if (
                status != ReaderStatus.ACTIVE
                        && status != ReaderStatus.BLOCKED
        ) {
            throw new ValidationException(
                    "Status faqat ACTIVE yoki BLOCKED bo'lishi mumkin"
            );
        }

        reader.setStatus(status);

        Reader updatedReader =
                readerRepository.save(reader);

        return readerMapper.toResponse(
                updatedReader
        );
    }

    public void delete(
            Long id
    ) {

        Reader reader =
                readerRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitobxon topilmadi"
                                )
                        );

        long activeLoanCount =
                loanRepository
                        .countByReaderIdAndStatus(
                                id,
                                LoanStatus.BORROWED
                        );

        if (activeLoanCount > 0) {

            throw new BusinessRuleException(
                    "Kitobxonda qaytarilmagan kitob bor, o'chirib bo'lmaydi"
            );
        }

        boolean hasLoanHistory =
                loanRepository.existsByReaderId(id);

        if (hasLoanHistory) {

            reader.setStatus(
                    ReaderStatus.INACTIVE
            );

            readerRepository.save(reader);

        } else {

            readerRepository.delete(reader);
        }
    }
}