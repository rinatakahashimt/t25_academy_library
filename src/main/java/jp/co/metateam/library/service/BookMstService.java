package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    public boolean bookcheck(BookMstDto bookMstDto,Model model){
        String bookTitle = bookMstDto.getTitle();
        String bookIsbn = bookMstDto.getIsbn();
        //エラーを貯めるリストを作る
        List<String> errTitleList = new ArrayList<>();
        List<String> errIsbnList = new ArrayList<>();

        if (StringUtils.isEmpty(bookTitle)){
            errTitleList.add("書籍名は必須です"); //書籍名の必須
            model.addAttribute("errTitle",errTitleList);
        }else if (bookTitle.length() > 255 ){
            errTitleList.add("書籍名は255文字以内で入力してください");  //書籍名の文字数
            model.addAttribute("errTitle",errTitleList);
        }

        if(StringUtils.isEmpty(bookIsbn)){
            errIsbnList.add("ISBNは必須です"); // ISBNの必須
            model.addAttribute("errIsbn",errIsbnList);
            // return !errTitleList.isEmpty() || !errIsbnList.isEmpty();
        }else if(bookIsbn.length() != 13 ){
            errIsbnList.add("ISBNは13桁で入力してください");   // ISBNの桁数
            model.addAttribute("errIsbn",errIsbnList);
        }

        // ISBNの半角数字
        if (!bookIsbn.matches("^[0-9]+$")) {
            errIsbnList.add("ISBNは半角数字で入力してください");
            model.addAttribute("errIsbn", errIsbnList);
        }

        if (!errTitleList.isEmpty() || !errIsbnList.isEmpty()){
            return true; //入力画面に戻す
        }
            return false;
     
    }
    //isbnの重複チェックするためにDBに接続するselectByIsbnメソッドを呼ぶ。



     public boolean checkIsbnEntry(BookMstDto bookMstDto,Model model) {
        String getIsbn = bookMstDto.getIsbn();
        List<String>errTitleList = new ArrayList<>() ;
        List<String>errIsbnList = new ArrayList<>() ;
        List<BookMst> bookMst  = this.bookMstRepository.selectByIsbn(getIsbn);
        if (!bookMst.isEmpty()) {
            errIsbnList.add("登録されているISBNです");
            model.addAttribute("errIsbn", errIsbnList);
            return true;}
        return false;
    }


    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // BookMstからbookへの変換
            BookMst book = new BookMst();

            book.setTitle(bookMstDto.getTitle());
            book.setIsbn(bookMstDto.getIsbn());
            
            // データベースへの保存
            this.bookMstRepository.save(book);
        } catch (Exception e) {
            throw e;
        }
    }
}



