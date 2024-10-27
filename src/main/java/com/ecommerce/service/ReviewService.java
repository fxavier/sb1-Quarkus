package com.ecommerce.service;

import com.ecommerce.domain.dto.ReviewDTO;
import com.ecommerce.domain.model.Review;
import com.ecommerce.domain.model.ReviewImage;
import com.ecommerce.domain.repository.ReviewRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ReviewService {
    
    @Inject
    ReviewRepository reviewRepository;
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<List<Review>> getProductReviews(Long productId) {
        return reviewRepository.findByProduct(productId);
    }
    
    public Uni<List<Review>> getUserReviews(String userId) {
        return reviewRepository.findByUser(userId);
    }
    
    @Transactional
    public Uni<Review> createReview(Long productId, String userId, ReviewDTO reviewDTO) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                Review review = new Review();
                review.setProduct(product);
                review.setUserId(userId);
                review.setRating(reviewDTO.getRating());
                review.setComment(reviewDTO.getComment());
                
                if (reviewDTO.getImageUrls() != null) {
                    reviewDTO.getImageUrls().forEach(url -> {
                        ReviewImage image = new ReviewImage();
                        image.setImageUrl(url);
                        image.setReview(review);
                        review.getImages().add(image);
                    });
                }
                
                return reviewRepository.persist(review)
                    .chain(savedReview -> reviewRepository.getAverageRating(productId)
                        .chain(avgRating -> {
                            product.setAverageRating(avgRating);
                            return productRepository.persist(product)
                                .map(p -> savedReview);
                        }));
            });
    }
    
    @Transactional
    public Uni<Review> updateReview(Long reviewId, String userId, ReviewDTO reviewDTO) {
        return reviewRepository.findById(reviewId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Review not found"))
            .chain(review -> {
                if (!review.getUserId().equals(userId)) {
                    return Uni.createFrom().failure(
                        new SecurityException("Not authorized to update this review"));
                }
                
                review.setRating(reviewDTO.getRating());
                review.setComment(reviewDTO.getComment());
                
                review.getImages().clear();
                if (reviewDTO.getImageUrls() != null) {
                    reviewDTO.getImageUrls().forEach(url -> {
                        ReviewImage image = new ReviewImage();
                        image.setImageUrl(url);
                        image.setReview(review);
                        review.getImages().add(image);
                    });
                }
                
                return reviewRepository.persist(review)
                    .chain(savedReview -> reviewRepository.getAverageRating(review.getProduct().getId())
                        .chain(avgRating -> {
                            review.getProduct().setAverageRating(avgRating);
                            return productRepository.persist(review.getProduct())
                                .map(p -> savedReview);
                        }));
            });
    }
    
    @Transactional
    public Uni<Void> deleteReview(Long reviewId, String userId) {
        return reviewRepository.findById(reviewId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Review not found"))
            .chain(review -> {
                if (!review.getUserId().equals(userId)) {
                    return Uni.createFrom().failure(
                        new SecurityException("Not authorized to delete this review"));
                }
                
                Long productId = review.getProduct().getId();
                return reviewRepository.delete(review)
                    .chain(() -> reviewRepository.getAverageRating(productId))
                    .chain(avgRating -> productRepository.findById(productId)
                        .chain(product -> {
                            product.setAverageRating(avgRating);
                            return productRepository.persist(product);
                        }))
                    .replaceWith(null);
            });
    }
    
    @Transactional
    public Uni<Review> voteHelpful(Long reviewId) {
        return reviewRepository.findById(reviewId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Review not found"))
            .chain(review -> {
                review.setHelpfulVotes(review.getHelpfulVotes() + 1);
                return reviewRepository.persist(review);
            });
    }
}