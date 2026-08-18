package com.mapvendor.module.business.controller;

import com.mapvendor.common.api.*;
import com.mapvendor.module.business.domain.*;
import com.mapvendor.module.business.dto.*;
import com.mapvendor.module.business.service.BusinessResourceService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/admin/businesses/{businessId}") @Tag(name="AdminBusinessResource")
public class AdminBusinessResourceController {
 private final BusinessResourceService service; public AdminBusinessResourceController(BusinessResourceService s){service=s;}
 @GetMapping("/cars") public ApiResponse<PageResult<BusinessResourceView>> cars(@PathVariable long businessId,@Valid PageQuery page,@RequestParam(required=false) ResourceStatus status){return ApiResponse.success(service.list(BusinessResourceType.CARS,businessId,page,status));}
 @GetMapping("/cars/{id}") public ApiResponse<BusinessResourceView> car(@PathVariable long businessId,@PathVariable long id){return ApiResponse.success(service.get(BusinessResourceType.CARS,businessId,id));}
 @PostMapping("/cars") public ApiResponse<BusinessResourceView> createCar(@PathVariable long businessId,@Valid @RequestBody CarSaveRequest r){return ApiResponse.success(service.createCar(businessId,r));}
 @PutMapping("/cars/{id}") public ApiResponse<BusinessResourceView> updateCar(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody CarSaveRequest r){return ApiResponse.success(service.updateCar(businessId,id,r));}
 @PatchMapping("/cars/{id}/status") public ApiResponse<BusinessResourceView> carStatus(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody ResourceStatusRequest r){return ApiResponse.success(service.status(BusinessResourceType.CARS,businessId,id,r.getStatus()));}
 @DeleteMapping("/cars/{id}") public ApiResponse<Void> deleteCar(@PathVariable long businessId,@PathVariable long id){service.delete(BusinessResourceType.CARS,businessId,id);return ApiResponse.success(null);}
 @GetMapping("/rooms") public ApiResponse<PageResult<BusinessResourceView>> rooms(@PathVariable long businessId,@Valid PageQuery page,@RequestParam(required=false) ResourceStatus status){return ApiResponse.success(service.list(BusinessResourceType.ROOMS,businessId,page,status));}
 @GetMapping("/rooms/{id}") public ApiResponse<BusinessResourceView> room(@PathVariable long businessId,@PathVariable long id){return ApiResponse.success(service.get(BusinessResourceType.ROOMS,businessId,id));}
 @PostMapping("/rooms") public ApiResponse<BusinessResourceView> createRoom(@PathVariable long businessId,@Valid @RequestBody RoomSaveRequest r){return ApiResponse.success(service.createRoom(businessId,r));}
 @PutMapping("/rooms/{id}") public ApiResponse<BusinessResourceView> updateRoom(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody RoomSaveRequest r){return ApiResponse.success(service.updateRoom(businessId,id,r));}
 @PatchMapping("/rooms/{id}/status") public ApiResponse<BusinessResourceView> roomStatus(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody ResourceStatusRequest r){return ApiResponse.success(service.status(BusinessResourceType.ROOMS,businessId,id,r.getStatus()));}
 @DeleteMapping("/rooms/{id}") public ApiResponse<Void> deleteRoom(@PathVariable long businessId,@PathVariable long id){service.delete(BusinessResourceType.ROOMS,businessId,id);return ApiResponse.success(null);}
 @GetMapping("/dishes") public ApiResponse<PageResult<BusinessResourceView>> dishes(@PathVariable long businessId,@Valid PageQuery page,@RequestParam(required=false) ResourceStatus status){return ApiResponse.success(service.list(BusinessResourceType.DISHES,businessId,page,status));}
 @GetMapping("/dishes/{id}") public ApiResponse<BusinessResourceView> dish(@PathVariable long businessId,@PathVariable long id){return ApiResponse.success(service.get(BusinessResourceType.DISHES,businessId,id));}
 @PostMapping("/dishes") public ApiResponse<BusinessResourceView> createDish(@PathVariable long businessId,@Valid @RequestBody DishSaveRequest r){return ApiResponse.success(service.createDish(businessId,r));}
 @PutMapping("/dishes/{id}") public ApiResponse<BusinessResourceView> updateDish(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody DishSaveRequest r){return ApiResponse.success(service.updateDish(businessId,id,r));}
 @PatchMapping("/dishes/{id}/status") public ApiResponse<BusinessResourceView> dishStatus(@PathVariable long businessId,@PathVariable long id,@Valid @RequestBody ResourceStatusRequest r){return ApiResponse.success(service.status(BusinessResourceType.DISHES,businessId,id,r.getStatus()));}
 @DeleteMapping("/dishes/{id}") public ApiResponse<Void> deleteDish(@PathVariable long businessId,@PathVariable long id){service.delete(BusinessResourceType.DISHES,businessId,id);return ApiResponse.success(null);}
}
