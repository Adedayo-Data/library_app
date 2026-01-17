package com.lawpavillion.bk.repository;

import com.lawpavillion.bk.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

}
