package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {
//    @Query(
//            """
//           select NEW org.datn.bookstation.dto.response.ParentCategoryResponse(c.id,c.categoryName,c.description,c.parentCategory.id,c.parentCategory.categoryName,c.parentCategory.description) from Category c
//"""
//    )
//    public List<ParentCategoryResponse> getAllParentCategoryRequests();


    @Query("SELECT c1.id , c1.categoryName , c1.description , " +
            "c1.status , c2.id , c2.categoryName , " +
            "c2.description AS childDesc, c2.status " +
            "FROM Category c1 LEFT JOIN Category c2 ON c1.id = c2.parentCategory.id")
    List<Category> findCategoryHierarchy();

    @Query("""
            select c from Category c where c.parentCategory is null 
            """)
    Category getByParentCategoryIsNull(@Param("id") Integer id);

    List<Category> findByStatus(Byte status);


    @Query("""
            select c from Category  c where c.id != :id and c.parentCategory.id is null 
            """)
    List<Category> getAllExceptByID(@Param("id") Integer id);

    @Query("""
            select c from Category  c where c.id != :id and c.parentCategory.id is not null 
            """)
    List<Category> getAllExceptByIdNotNull(@Param("id") Integer id);

    @Query("""
                    select c from Category c where c.parentCategory.id is null 
            """)
    List<Category> getAllByParentIsNull();

    @Query("""
                    select c from Category c where c.parentCategory.id is not null 
            """)
    List<Category> getAllByParentIsNotNull();

    @Query("""
            select c from Category c where c.parentCategory.id=:id
            """)
    List<Category> getALlByParentId(@Param("id") Integer id);


    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE UPPER(TRIM(c.categoryName)) = UPPER(TRIM(:categoryName))")
    boolean existsByCategoryNameIgnoreCase(@Param("categoryName") String categoryName);


    // Trong CategoryRepository
    Category findByCategoryNameIgnoreCase(String categoryName);

    boolean existsByParentCategoryId(Integer id);
}
