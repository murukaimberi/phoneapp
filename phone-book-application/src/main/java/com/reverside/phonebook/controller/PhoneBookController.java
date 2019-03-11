package com.reverside.phonebook.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.reverside.phonebook.exception.ResourceNotFoundException;
import com.reverside.phonebook.model.Entry;
import com.reverside.phonebook.model.PhoneBook;
import com.reverside.phonebook.model.Search;
import com.reverside.phonebook.repository.EntryRepository;
import com.reverside.phonebook.repository.PhoneBookRepository;
import com.reverside.phonebook.service.EntryService;
import com.reverside.phonebook.service.PhoneBookService;

@Controller
public class PhoneBookController {
	
	private PhoneBookService bookService;
	
	private EntryService entryService;

	@Autowired
	public PhoneBookController(PhoneBookService phoneBookService,
			EntryService entryService) {
		this.bookService = phoneBookService;
		this.entryService = entryService;
	}

	@GetMapping({ "/", "/home", "/index" })
	public String home(Model model) {
		model.addAttribute("description",
				"This is an applications that will allow you to create and save phone entries");
		model.addAttribute("search", new Search());
		return "home";
	}

	@GetMapping("/phone-books")
	public String getPhoneBooks(Model model, @RequestParam(defaultValue = "0")int page) {

		Pageable requestedPage = PageRequest.of(page, 3);
		Page<PhoneBook> phoneBooks = bookService.findAll(requestedPage);
		model.addAttribute("phoneBooks", phoneBooks);
		model.addAttribute("currentPage", page);
		model.addAttribute("phoneBook", new PhoneBook());
		model.addAttribute("description", "List of Phone Books");
		model.addAttribute("search", new Search());
		return "phone-books";
	}

	@PostMapping("/phone-books/add")
	public String addPhoneBook(@Valid PhoneBook phoneBook) {
		bookService.save(phoneBook);
		return "redirect:/phone-books";
	}

	@GetMapping("/phone-books/findOne")
	@ResponseBody
	public PhoneBook getPhoneBook(Long id, Model model) {
		return bookService.findById(id).get();
	}

	@GetMapping("/phone-books/view/{phoneBookId}")
	public String getEntries(@PathVariable Long phoneBookId, Model model) {
		List<Entry> entries = new ArrayList<>();

		List<Entry> blankEntries = new ArrayList<>();

		entries = entryService.findByPhoneBookId(phoneBookId).orElse(blankEntries);

		PhoneBook tempPhoneBook = bookService.getOne(phoneBookId);

		model.addAttribute("phoneBook", tempPhoneBook);

		model.addAttribute("search", new Search());
		
		model.addAttribute("description", "List of Phone book entries");

		model.addAttribute("entry", new Entry());

		model.addAttribute("entries", entries);

		return "phone-entries";
	}

	@GetMapping("/phone-books/entries/findOne")
	@ResponseBody
	public Entry getEntry(Long id) {
		return entryService.findById(id).get();
	}

	@PostMapping("/phone-books/entries")
	public String addEntry(@Valid Entry entry) {
		entryService.save(entry);
		return "redirect:/phone-books/view/" + entry.getPhoneBookId();
	}

	@GetMapping("/phone-books/entries/delete/{entryId}")
	public String deleteEntry(@PathVariable Long entryId) {
		Entry entry = entryService.findById(entryId)
				.orElseThrow(() -> new ResourceNotFoundException("Entry", "id", entryId));
		entryService.delete(entry);
		return "redirect:/phone-books/view/" + entry.getPhoneBookId();
	}

	@GetMapping("/phone-books/delete/{phoneBookId}")
	public String deletePhoneBook(@PathVariable Long phoneBookId) {
		PhoneBook phoneBook = bookService.findById(phoneBookId)
				.orElseThrow(() -> new ResourceNotFoundException("PhoneBook", "id", phoneBookId));
		bookService.delete(phoneBook);
		return "redirect:/phone-books";
	}

	/*
	 * Search functionality
	 */
	@GetMapping("/phone-books/search/{name}/name")
	public String searchByName(@PathVariable String name, Model model) {
		List<Entry> entries = new ArrayList<>();

		List<Entry> blankEntries = new ArrayList<>();

		entries = entryService.findByNameLike(name).orElse(blankEntries);


		model.addAttribute("description", "List of Phone book entries");
		
		model.addAttribute("search", new Search());

		model.addAttribute("entries", entries);

		return "searched-entries";
	}

	@GetMapping("/phone-books/search/{phoneNumber}/phone-number")
	public String searchByPhoneNumber(@PathVariable String phoneNumber, Model model) {
		List<Entry> entries = new ArrayList<>();

		List<Entry> blankEntries = new ArrayList<>();

		entries = entryService.findByPhoneNumberLike(phoneNumber).orElse(blankEntries);


		model.addAttribute("description", "List of Phone book entries");

		model.addAttribute("search", new Search());

		model.addAttribute("entries", entries);

		return "searched-entries";
	}

	@PostMapping("/phone-books/search/name")
	public String searchName(Search search) {
		return "redirect:/phone-books/search/" + search.getName() + "/name";
	}

	@PostMapping("/phone-books/search/phoneNumber")
	public String searchPhoneNumber(Search search) {
		return "redirect:/phone-books/search/" + search.getPhoneNumber() + "/phone-number";
	}

}
