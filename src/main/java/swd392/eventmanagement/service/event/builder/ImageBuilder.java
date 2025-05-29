package swd392.eventmanagement.service.event.builder;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Image;
import swd392.eventmanagement.repository.ImageRepository;

@Component
public class ImageBuilder {

    private final ImageRepository imageRepository;

    public ImageBuilder(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Updates the images for an event
     * Using PUT semantics - removes all existing images and adds new ones if
     * provided
     * 
     * @param event     Event entity to update
     * @param imageUrls List of image URLs to associate with the event, can be null
     *                  or empty
     */
    public void updateEventImages(Event event, Set<String> imageUrls) {
        // Delete all existing images associated with this event
        imageRepository.deleteByEvent(event);

        // Add new images if provided
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Set<Image> images = new HashSet<>();
            for (String imageUrl : imageUrls) {
                Image image = new Image();
                image.setEvent(event);
                image.setUrl(imageUrl);
                images.add(image);
            }
            imageRepository.saveAll(images);
        }
    }
}
