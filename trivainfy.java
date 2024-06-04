package com.infosys.trivainfy.controller;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.infosys.trivainfy.TrivaInfyApplication;
import com.infosys.trivainfy.dto.BookingDTO;
import com.infosys.trivainfy.dto.HotelDTO;
import com.infosys.trivainfy.dto.VendorDTO;
import com.infosys.trivainfy.exception.TrivaInfyException;
import com.infosys.trivainfy.service.TrivaInfyService;
//Implement this class as per the instructions given in Question Paper.
@RestController
@Validated
@RequestMapping
public class TrivaInfyController {
  @Autowired
  private TrivaInfyService trivaInfyService;
  @GetMapping(value="/hotels/{hotelNameSearchKey}")
  public List<HotelDTO> searchHotels(@PathVariable String hotelNameSearchKey) throws TrivaInfyException{
    List<HotelDTO> searchHotels = trivaInfyService.searchHotels(hotelNameSearchKey.trim());
    return searchHotels;
  }
  @GetMapping(value="/vendors/{vendorNameSearchKey}")
  public List<VendorDTO> searchVendors(@PathVariable String vendorNameSearchKey) throws TrivaInfyException{
    List<VendorDTO> searchVend = trivaInfyService.searchVendors(vendorNameSearchKey.trim());
    return searchVend;
  }
  @PostMapping(value="/booking")
   public String bookHotel(@Valid @RequestBody BookingDTO bookingDto) throws TrivaInfyException{
    return trivaInfyService.bookHotel(bookingDto);
  }
  @PutMapping(value="/booking/{bookingId}/{noOfRoomsNew}")
  public String updateBooking(@PathVariable @Min(value=1,message="{booking.bookingid.invalid}") Integer bookingId,
      @PathVariable @Min(value=1,message="{booking.noofrooms.invalid}") Integer noOfRoomsNew) throws TrivaInfyException {
    return trivaInfyService.updateBooking(bookingId, noOfRoomsNew);
}
  @DeleteMapping(value="/booking/{bookingId}")
  public String cancelBooking(@PathVariable @Min(value=1,message="{booking.bookingid.invalid}") Integer bookingId) throws TrivaInfyException {
    return trivaInfyService.cancelBooking(bookingId);
  }
}
/////////////////////////////////////////////
package com.infosys.trivainfy.service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infosys.trivainfy.TrivaInfyApplication;
import com.infosys.trivainfy.dto.BookingDTO;
import com.infosys.trivainfy.dto.HotelDTO;
import com.infosys.trivainfy.dto.VendorDTO;
import com.infosys.trivainfy.entity.Booking;
import com.infosys.trivainfy.entity.Hotel;
import com.infosys.trivainfy.entity.Vendor;
import com.infosys.trivainfy.exception.TrivaInfyException;
import com.infosys.trivainfy.repository.BookingRepository;
import com.infosys.trivainfy.repository.HotelRepository;
import com.infosys.trivainfy.repository.VendorRepository;
import com.infosys.trivainfy.utilities.TrivaInfyConstants;
@Service
@PropertySource("classpath:ValidationMessages.properties")
public class TrivaInfyServiceImpl implements TrivaInfyService {
  @Autowired
  private HotelRepository hotelRepository;
  @Autowired
  private VendorRepository vendorRepository;
  @Autowired
  private BookingRepository bookingRepository;
  @Autowired
  private Environment environment;
  //Implement this method as per the instructions given in Question Paper.
  @Override
  public List<HotelDTO> searchHotels(String hotelNameSearchKey) throws TrivaInfyException {
    List<Hotel> list = hotelRepository.findByHotelNameContainingIgnoreCase(hotelNameSearchKey);
    if(list.isEmpty()) {
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_SEARCH_HOTEL_INVALID.toString());
    }
    List<HotelDTO> hotelList = new ArrayList<>();
    for(Hotel hotel:list) {
      if(hotel.getHotelStatus().equals("A")) {
        hotelList.add(HotelDTO.entityToDTOConvertor(hotel));
      }
          }
    if(hotelList.isEmpty()) {
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_SEARCH_HOTEL_INVALID.toString());
    }
    return hotelList;
  }
  public List<VendorDTO> searchVendors(String vendorNameSearchKey) throws TrivaInfyException {
    List<Vendor> vendorListFromRepo = vendorRepository.findByVendorNameContainingIgnoreCase(vendorNameSearchKey);
    if (vendorListFromRepo.isEmpty()) {
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_SEARCH_VENDOR_INVALID.toString());
    }
    List<VendorDTO> vendorDtoList = new ArrayList<>();
    for (Vendor vendor : vendorListFromRepo) {
      VendorDTO vendorDto = new VendorDTO();
      vendorDto.setVendorId(vendor.getVendorId());
      vendorDto.setVendorName(vendor.getVendorName());
      List<HotelDTO> hotelDtoList = new ArrayList<>();
      for (Hotel hotel : vendor.getHotels()) {
        if ("A".equals(hotel.getHotelStatus())) {
          HotelDTO hotelDto = new HotelDTO();
          hotelDto.setHotelId(hotel.getHotelId());
          hotelDto.setHotelName(hotel.getHotelName());
          hotelDto.setLocation(hotel.getLocation());
          hotelDto.setRoomCharge(hotel.getRoomCharge());
          hotelDto.setAmenities(hotel.getAmenities());
          hotelDto.setRoomsAvailable(hotel.getRoomsAvailable());
          hotelDto.setHotelStatus(hotel.getHotelStatus());
          hotelDtoList.add(hotelDto);
        }
      }
      vendorDto.setHotels(hotelDtoList);
      vendorDtoList.add(vendorDto);
    }
    return vendorDtoList;
  }
  @Transactional
  public String bookHotel(BookingDTO bookingDto) throws TrivaInfyException {
    Hotel hotel = hotelRepository.findByHotelName(bookingDto.getHotelName());
    if (hotel == null)
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_HOTEL_INVALID.toString());
    if (hotel.getRoomsAvailable() < bookingDto.getNoOfRooms())
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_HOTEL_ROOMS_UNAVAILABLE.toString());
    List<Vendor> vendors = hotel.getVendors();
    for (Vendor vendor : vendors) {
      if (vendor.getVendorName().equals(bookingDto.getVendorName())) {
        Double total = bookingDto.getNoOfRooms() * hotel.getRoomCharge();
        Booking booking = new Booking();
        booking.setHotelId(hotel.getHotelId());
        booking.setVendorId(vendor.getVendorId());
        booking.setTotalAmount(total);
        int bookingId = bookingRepository.save(booking).getBookingId();
        hotel.setRoomsAvailable(hotel.getRoomsAvailable() - bookingDto.getNoOfRooms());
        return environment.getProperty(TrivaInfyConstants.TRIVAINFY_BOOKING_SUCCESS_1.toString()) + bookingId
            + environment.getProperty(TrivaInfyConstants.TRIVAINFY_BOOKING_SUCCESS_2.toString()) + total;
      }
    }
    throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_BOOKING_INVALID_VENDOR_NAME.toString());
  }
  @Transactional
  public String updateBooking(Integer bookingId, int noOfRoomsNew) throws TrivaInfyException {
    Booking booking = bookingRepository.findByBookingId(bookingId);
    if (booking == null) {
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_BOOKINGID_INVALID.toString());
    } else {
      String hotelId = booking.getHotelId();
      Hotel hotel = hotelRepository.findByHotelId(hotelId);
      int roomsAlreadyBooked = (int) (booking.getTotalAmount().doubleValue()
          / hotel.getRoomCharge().doubleValue());
      if (roomsAlreadyBooked == noOfRoomsNew)
        throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_BOOKINGID_NOOFROOMS_INVALID.toString());
      if (hotel.getRoomsAvailable() + roomsAlreadyBooked < noOfRoomsNew)
        throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_HOTEL_ROOMS_UNAVAILABLE.toString());
      double total = noOfRoomsNew * hotel.getRoomCharge();
      hotel.setRoomsAvailable(hotel.getRoomsAvailable() + roomsAlreadyBooked - noOfRoomsNew);
      booking.setTotalAmount(total);
      return environment.getProperty(TrivaInfyConstants.TRIVAINFY_UPDATE_SUCCESS.toString()) + total;
    }
  }
  @Transactional
  public String cancelBooking(Integer bookingId) throws TrivaInfyException {
    Booking booking = bookingRepository.findByBookingId(bookingId);
    if (booking == null) {
      throw new TrivaInfyException(TrivaInfyConstants.TRIVAINFY_BOOKINGID_INVALID.toString());
    } else {
      String hotelId = booking.getHotelId();
      Hotel hotel = hotelRepository.findByHotelId(hotelId);
      int roomsAlreadyBooked = (int) (booking.getTotalAmount().doubleValue()
          / hotel.getRoomCharge().doubleValue());
      hotel.setRoomsAvailable(hotel.getRoomsAvailable() + roomsAlreadyBooked);
      bookingRepository.deleteById(bookingId);
    }
    return environment.getProperty(TrivaInfyConstants.TRIVAINFY_DELETE_SUCCESS.toString());
  }
}
///////////////////////////////////////////////
package com.infosys.trivainfy.dto;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
//Annotate the attributes of this class with appropriate annotations from Java Bean Validation API.
public class BookingDTO {
  @NotNull(message="{booking.hotelname.invalid}")
  @NotBlank(message="{booking.hotelname.invalid}")
  private String hotelName;
  @NotNull(message="{booking.vendorname.invalid}")
  @NotBlank(message="{booking.vendorname.invalid}")
  private String vendorName;
  @Min(value=1,message="{booking.noofrooms.invalid}")
  private int noOfRooms;
  public String getHotelName() {
    return hotelName;
  }
  public void setHotelName(String hotelName) {
    this.hotelName = hotelName;
  }
  public String getVendorName() {
    return vendorName;
  }
  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }
  public int getNoOfRooms() {
    return noOfRooms;
  }
  public void setNoOfRooms(int noOfRooms) {
    this.noOfRooms = noOfRooms;
  }
  @Override
  public String toString() {
    return "BookingDTO [hotelName=" + hotelName + ", vendorName=" + vendorName + ", noOfRooms=" + noOfRooms + "]";
  }
  ///////////////////////////////
  package com.infosys.trivainfy.utilities;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.infosys.trivainfy.exception.TrivaInfyException;
//Implement this class as per the instructions given in Question Paper.
/**
 * The Class ExceptionControllerAdvice.
 */
@RestControllerAdvice
public class ExceptionControllerAdvice {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired
  private Environment environment;
  /**
   * Exception handler for general exception "Exception".
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorInfo> generalExceptionHandler(Exception ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.setErrorMsg(environment.getProperty(TrivaInfyConstants.TRIVAINFY_EXCEPTIONMSG_GENERAL.toString()));
    return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
  }
  /**
   * Exception handler for TrivaInfyException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(TrivaInfyException.class)
  public ResponseEntity<ErrorInfo> trivaInfyExceptionHandler(TrivaInfyException ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.BAD_REQUEST.value());
    error.setErrorMsg(environment.getProperty(ex.getMessage()));
    return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
  }
  /**
   * Exception handler for MethodArgumentNotValidException and
   * ConstraintViolationException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
  public ResponseEntity<ErrorInfo> exceptionHandler(Exception ex) {
    logger.error(ex.getMessage(), ex);
    String errorMsg;
    if (ex instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
      errorMsg = manve.getBindingResult().getAllErrors().stream().map(x -> x.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else {
      ConstraintViolationException cve = (ConstraintViolationException) ex;
      errorMsg = cve.getConstraintViolations().stream().map(x -> x.getMessage())
          .collect(Collectors.joining(", "));
    }
    ErrorInfo errorInfo = new ErrorInfo();
    errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
    errorInfo.setErrorMsg(errorMsg);
    return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
  }
}
package com.infosys.trivainfy.utilities;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.infosys.trivainfy.exception.TrivaInfyException;
//Implement this class as per the instructions given in Question Paper.
/**
 * The Class ExceptionControllerAdvice.
 */
@RestControllerAdvice
public class ExceptionControllerAdvice {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired
  private Environment environment;
  /**
   * Exception handler for general exception "Exception".
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorInfo> generalExceptionHandler(Exception ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.setErrorMsg(environment.getProperty(TrivaInfyConstants.TRIVAINFY_EXCEPTIONMSG_GENERAL.toString()));
    return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
  }
  /**
   * Exception handler for TrivaInfyException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(TrivaInfyException.class)
  public ResponseEntity<ErrorInfo> trivaInfyExceptionHandler(TrivaInfyException ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.BAD_REQUEST.value());
    error.setErrorMsg(environment.getProperty(ex.getMessage()));
    return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
  }
  /**
   * Exception handler for MethodArgumentNotValidException and
   * ConstraintViolationException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
  public ResponseEntity<ErrorInfo> exceptionHandler(Exception ex) {
    logger.error(ex.getMessage(), ex);
    String errorMsg;
    if (ex instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
      errorMsg = manve.getBindingResult().getAllErrors().stream().map(x -> x.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else {
      ConstraintViolationException cve = (ConstraintViolationException) ex;
      errorMsg = cve.getConstraintViolations().stream().map(x -> x.getMessage())
          .collect(Collectors.joining(", "));
    }
    ErrorInfo errorInfo = new ErrorInfo();
    errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
    errorInfo.setErrorMsg(errorMsg);
    return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
  }
}package com.infosys.trivainfy.utilities;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.infosys.trivainfy.exception.TrivaInfyException;
//Implement this class as per the instructions given in Question Paper.
/**
 * The Class ExceptionControllerAdvice.
 */
@RestControllerAdvice
public class ExceptionControllerAdvice {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired
  private Environment environment;
  /**
   * Exception handler for general exception "Exception".
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorInfo> generalExceptionHandler(Exception ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.setErrorMsg(environment.getProperty(TrivaInfyConstants.TRIVAINFY_EXCEPTIONMSG_GENERAL.toString()));
    return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
  }
  /**
   * Exception handler for TrivaInfyException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler(TrivaInfyException.class)
  public ResponseEntity<ErrorInfo> trivaInfyExceptionHandler(TrivaInfyException ex) {
    logger.error(ex.getMessage(),ex);
    ErrorInfo error = new ErrorInfo();
    error.setErrorCode(HttpStatus.BAD_REQUEST.value());
    error.setErrorMsg(environment.getProperty(ex.getMessage()));
    return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
  }
  /**
   * Exception handler for MethodArgumentNotValidException and
   * ConstraintViolationException.
   *
   * @param exception
   * @return the error information with error code and error message
   */
  @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
  public ResponseEntity<ErrorInfo> exceptionHandler(Exception ex) {
    logger.error(ex.getMessage(), ex);
    String errorMsg;
    if (ex instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
      errorMsg = manve.getBindingResult().getAllErrors().stream().map(x -> x.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else {
      ConstraintViolationException cve = (ConstraintViolationException) ex;
      errorMsg = cve.getConstraintViolations().stream().map(x -> x.getMessage())
          .collect(Collectors.joining(", "));
    }
    ErrorInfo errorInfo = new ErrorInfo();
    errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
    errorInfo.setErrorMsg(errorMsg);
    return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
  }
}
