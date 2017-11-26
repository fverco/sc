package com.github._1element.sc.controller; //NOSONAR

import com.github._1element.sc.dto.ImagesCameraSummaryResult;
import com.github._1element.sc.repository.SurveillanceImageRepository;
import com.github._1element.sc.service.SurveillanceService;
import com.github._1element.sc.utils.URIConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping(URIConstants.FEED_ROOT)
public class SurveillanceFeedController {

  private SurveillanceService surveillanceService;

  private SurveillanceImageRepository imageRepository;

  @Value("${sc.feed.baseurl}")
  private String feedBaseUrl;

  @Autowired
  public SurveillanceFeedController(SurveillanceService surveillanceService,
                                    SurveillanceImageRepository imageRepository) {
    this.surveillanceService = surveillanceService;
    this.imageRepository = imageRepository;
  }

  /**
   * Renders RSS status feed with number of recordings and most recent date.
   *
   * @param model the spring model
   * @return rendered RSS feed
   */
  @GetMapping(URIConstants.FEED_STATUS)
  public String statusfeed(Model model) {
    Long countAllImages = imageRepository.countAllImages();
    LocalDateTime mostRecentImageDate = surveillanceService.getMostRecentImageDate();

    model.addAttribute("countRecordings", countAllImages);
    model.addAttribute("mostRecentImageDate", mostRecentImageDate);

    return "feed-status";
  }

  /**
   * Renders RSS status feed displaying a summary for each camera.
   *
   * @param model the spring model
   * @return rendered RSS feed
   */
  @GetMapping(URIConstants.FEED_CAMERAS)
  public String camerasfeed(Model model) {
    List<ImagesCameraSummaryResult> imagesCameraSummaryResult = surveillanceService.getImagesCameraSummary();

    model.addAttribute("imagesCameraSummaryResult", imagesCameraSummaryResult);

    return "feed-cameras";
  }

  @ModelAttribute
  public void populateBaseUrl(Model model) {
    model.addAttribute("baseUrl", feedBaseUrl + URIConstants.RECORDINGS);
  }

}
