DTO CLASSES:--


--------
Ride dto:--
package com.infosys.infyride.dto;
import java.time.LocalDateTime;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.infosys.infyride.entity.RideEntity;
//Annotate the attributes of this class with appropriate annotations from Java Bean Validation API.
/**
 * The Class RideDTO.
 *
 */
public class RideDTO {
 /** The pick up location */
  @NotBlank(message="{ride.pickuplocation.notpresent}")
  @Pattern(regexp="([a-zA-Z0-9,-/\\s])+", message= "{ride.pickuplocation.invalid}")
  private String pickupLocation;
 //
  @NotBlank(message="{ride.droplocation.notpresent}")
  @Pattern(regexp="([a-zA-Z0-9,-/\\s])+" , message= "{ride.droplocation.invalid}")
  private String dropLocation;
// /** The ride date time */
  @NotNull(message="{ride.datetime.notpresent}")
  @Future(message="{ride.datetime.invalid}")
 private LocalDateTime rideDateTime;
 /**
  * Gets the pickup location.
  *
  * @return the pickup location
  */
 public String getPickupLocation() {
  return pickupLocation;
 }
 /**
  * Sets the pickup location.
  *
  * @param pickupLocation the pickup location
  */
 public void setPickupLocation(String pickupLocation) {
  this.pickupLocation = pickupLocation;
 }
 /**
  * Gets the drop location.
  *
  * @return the drop location
  */
 public String getDropLocation() {
  return dropLocation;
 }
 /**
  * Sets the drop location.
  *
  * @param dropLocation the drop location
  */
 public void setDropLocation(String dropLocation) {
  this.dropLocation = dropLocation;
 }
 /**
  * Gets the ride date time.
  *
  * @return the ride date time
  */
 public LocalDateTime getRideDateTime() {
  return rideDateTime;
 }
 /**
  * Sets the ride date time.
  *
  * @param rideDateTime the ride date time
  */
 public void setRideDateTime(LocalDateTime rideDateTime) {
  this.rideDateTime = rideDateTime;
 }
 /**
  * Converts a RideDTO to RideEntity
  *
  * @param rideDTO
  */
 public static RideEntity prepareRideEntity(RideDTO rideDTO) {
  RideEntity rideEntity = new RideEntity();
  rideEntity.setPickupLocation(rideDTO.getPickupLocation());
  rideEntity.setDropLocation(rideDTO.getDropLocation());
  rideEntity.setRideDateTime(rideDTO.getRideDateTime().toString());
  rideEntity.setReasonForCancellation("NA");
  return rideEntity;
 }
 @Override
 public String toString() {
  return "RideDTO [pickupLocation=" + pickupLocation + ", dropLocation=" + dropLocation + ", rideDateTime="
    + rideDateTime + "]";
 }
}
==============================================================================================================================================
package com.infosys.infyride.dto;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
//Annotate the attribute of this class with appropriate annotations from Java Bean Validation API.
/**
 * The Class CancelBookingDTO.
 *
 */
public class CancelBookingDTO {
 /** The reason of cancellation. */
 @NotNull(message="{ride.reasonforcancellation.notpresent}")
 @Pattern(regexp = "(?=.*[a-zA-Z0-9].*)[a-zA-Z0-9.,! ]*",message="{ride.reasonforcancellation.invalid}")
 private String reasonForCancellation;
 /**
  * Gets the reason for cancellation.
  *
  * @return the reason for cancellation
  */
 public String getReasonForCancellation() {
  return reasonForCancellation;
 }
 /**
  * Sets the reason for cancellation.
  *
  * @param reasonForCancellation the reasonForCancellation
  */
 public void setReasonForCancellation(String reasonForCancellation) {
  this.reasonForCancellation = reasonForCancellation;
 }
 @Override
 public String toString() {
  return "CancelBookingDTO [reasonForCancellation=" + reasonForCancellation + "]";
 }
}
--------------------------------------------------------------------------------------------------------
CONTROLLER:-
-----------
package com.infosys.infyride.controller;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
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
import com.infosys.infyride.dto.CancelBookingDTO;
import com.infosys.infyride.dto.RideDTO;
import com.infosys.infyride.exception.InfyRideException;
import com.infosys.infyride.service.InfyRideService;
//Implement this class as per the instructions given in Question Paper.
@RestController
//implement as per in question paper
@Validated
public class InfyRideController {
 @Autowired
 private InfyRideService infyRideService;
 @GetMapping(value="/ride/{pickupLocation}/{dropLocation}")
 public String getEstimatedFare(@PathVariable String pickupLocation,@PathVariable String dropLocation) throws InfyRideException {
  String successMessage=infyRideService.getEstimatedFare(pickupLocation.trim(),dropLocation.trim());
  return successMessage;
 }
 @PostMapping(value="/ride")
 public String bookRide(@Valid @RequestBody RideDTO rideDTO) throws InfyRideException {
  String successMessage=infyRideService.bookRide(rideDTO);
  return successMessage;
 }
 @PutMapping(value="/ride/{rideId}/{newPickupLocation}")
 public String updateRide(@PathVariable @Min(value=1,message="{ride.rideid.invalid}") int rideId,@PathVariable String newPickupLocation) throws InfyRideException {
  String successMessage=infyRideService.updateRide(rideId,newPickupLocation);
  return successMessage;
 }
 @DeleteMapping(value="/ride/{rideId}")
 public String cancelRide(@PathVariable("rideId") @Min(value=1,message="{ride.rideid.invalid}") int rideId, @Valid @RequestBody CancelBookingDTO cancelBookingDTO ) throws InfyRideException{
  String successMessage=infyRideService.cancelRide(rideId,cancelBookingDTO);
  return successMessage;
 }
 }
---------------------------------------------------------------------------
----------------------------------------------------------------------------
SERVICE IMPL:-
------------
package com.infosys.infyride.service;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infosys.infyride.dto.CancelBookingDTO;
import com.infosys.infyride.dto.RideDTO;
import com.infosys.infyride.entity.FareEntity;
import com.infosys.infyride.entity.RideEntity;
import com.infosys.infyride.exception.InfyRideException;
import com.infosys.infyride.repository.FareRepository;
import com.infosys.infyride.repository.RideRepository;
import com.infosys.infyride.utilities.InfyRideConstants;
@Service
@PropertySource("classpath:ValidationMessages.properties")
public class InfyRideServiceImpl implements InfyRideService {
 @Autowired
 private RideRepository rideRepository;
 @Autowired
 private FareRepository fareRepository;
 @Autowired
 private Environment environment;
 private static final String CANCELLED = "CANCELLED";
 @Override
 public String getEstimatedFare(String pickupLocation, String dropLocation) throws InfyRideException {
  FareEntity fareEntity = fareRepository.getByPickupLocationIgnoreCaseAndDropLocationIgnoreCase(pickupLocation,
    dropLocation);
  if (fareEntity == null)
   throw new InfyRideException(InfyRideConstants.INFYRIDE_PICKUPTODROPLOCATION_NOT_FOUND.toString());
  return environment.getProperty(InfyRideConstants.INFYRIDE_GETESTIMATED_FARE_SUCCESS.toString())
    + fareEntity.getFare();
 }
 @Override
 public String bookRide(RideDTO rideDTO) throws InfyRideException {
  FareEntity fareDetails = fareRepository.getByPickupLocationIgnoreCaseAndDropLocationIgnoreCase(
    rideDTO.getPickupLocation(), rideDTO.getDropLocation());
  if (fareDetails == null)
   throw new InfyRideException(InfyRideConstants.INFYRIDE_PICKUPTODROPLOCATION_NOT_FOUND.toString());
  RideEntity rideEntity = RideDTO.prepareRideEntity(rideDTO);
  rideEntity.setStatus("BOOKED");
  rideEntity.setTotalFare(fareDetails.getFare());
  rideRepository.save(rideEntity);
  return environment.getProperty(InfyRideConstants.INFYRIDE_BOOKING_SUCCESS.toString())
    + rideEntity.getTotalFare();
 }
 @Override
 @Transactional
 public String updateRide(int rideId, String newPickupLocation) throws InfyRideException {
  Optional<RideEntity> rideEntityOpt = rideRepository.findById(rideId);
  if (!rideEntityOpt.isPresent())
   throw new InfyRideException(InfyRideConstants.INFYRIDE_RIDEID_NOT_FOUND.toString());
  RideEntity rideEntity = rideEntityOpt.get();
  if (rideEntity.getStatus().equals("COMPLETED"))
   throw new InfyRideException(InfyRideConstants.INFYRIDE_UPDATE_RIDE_ALREADY_COMPLETED.toString());
  if (rideEntity.getStatus().equals(CANCELLED))
   throw new InfyRideException(InfyRideConstants.INFYRIDE_UPDATE_RIDE_ALREADY_CANCELLED.toString());
  newPickupLocation = newPickupLocation.trim();
  if (rideEntity.getPickupLocation().equalsIgnoreCase(newPickupLocation))
   throw new InfyRideException(InfyRideConstants.INFYRIDE_OLDANDNEWPICKUPLOCATION_SAME.toString());
  FareEntity fareEntity = fareRepository.getByPickupLocationIgnoreCaseAndDropLocationIgnoreCase(newPickupLocation,
    rideEntity.getDropLocation());
  if (fareEntity == null)
   throw new InfyRideException(InfyRideConstants.INFYRIDE_PICKUPTODROPLOCATION_NOT_FOUND.toString());
  rideEntity.setPickupLocation(fareEntity.getPickupLocation());
  rideEntity.setTotalFare(fareEntity.getFare());
  return environment.getProperty(InfyRideConstants.INFYRIDE_UPDATE_SUCCESS1.toString()) + fareEntity.getPickupLocation() + " " +
    environment.getProperty(InfyRideConstants.INFYRIDE_UPDATE_SUCCESS2.toString()) + rideEntity.getTotalFare();
 }
 //Implement this method as per the instructions given in Question Paper.
 @Override
 @Transactional
 public String cancelRide(int rideId, CancelBookingDTO cancelBookingDTO) throws InfyRideException {
  Optional<RideEntity> ridentity=rideRepository.findById(rideId);
  if(!ridentity.isPresent()) {
   throw new InfyRideException(InfyRideConstants.INFYRIDE_RIDEID_NOT_FOUND.toString());
  }
  RideEntity rideEntity=ridentity.get();
  if(rideEntity.getStatus().equals("COMPLETED")) {
   throw new InfyRideException(InfyRideConstants.INFYRIDE_CANCEL_RIDE_ALREADY_COMPLETED.toString());
  }
  if(rideEntity.getStatus().equals(CANCELLED)) {
   throw new InfyRideException(InfyRideConstants.INFYRIDE_CANCEL_RIDE_ALREADY_CANCELLED.toString());
  }
 rideEntity.setStatus(CANCELLED);
 rideEntity.setReasonForCancellation(cancelBookingDTO.getReasonForCancellation());
  return environment.getProperty(InfyRideConstants.INFYRIDE_CANCEL_SUCCESS.toString());
 }
}
************************************
----------------------------------------------------------------------
EXCEPTIONCONTROLLERADVICE:-
--------------------------
@RestControllerAdvice
public class ExceptionControllerAdvice {
 private final Logger logger= LoggerFactory.getLogger(this.getClass());
 @Autowired
 private Environment environment;
 @ExceptionHandler(Exception.class)
 public ResponseEntity<ErrorInfo> generalExceptionHandler(Exception ex){
  logger.error(ex.getMessage(),ex);
  ErrorInfo errorInfo=new ErrorInfo();
  errorInfo.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
  errorInfo.setErrorMsg(environment.getProperty(InfyRideConstants.INFYRIDE_EXCEPTIONMSG_GENERAL.toString()));
  return new ResponseEntity<>(errorInfo,HttpStatus.INTERNAL_SERVER_ERROR);
 }
 @ExceptionHandler(InfyRideException.class)
 public ResponseEntity<ErrorInfo> infyRideExceptionHandler(InfyRideException ex){
  logger.error(ex.getMessage(),ex);
  ErrorInfo errorInfo=new ErrorInfo();
  errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
  errorInfo.setErrorMsg(environment.getProperty(ex.getMessage()));
  return new ResponseEntity<>(errorInfo,HttpStatus.BAD_REQUEST);
 }
 @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
 //annotate above condition in method-3/last method
