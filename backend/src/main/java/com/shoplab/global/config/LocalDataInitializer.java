package com.shoplab.global.config;

import com.shoplab.domain.category.domain.Category;
import com.shoplab.domain.category.domain.CategoryRepository;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private int imageSeq = 0;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return; // 이미 데이터가 있으면 건너뜀
        }

        Category clothing = save("의류", null, 1);
        Category tops = save("상의", clothing, 1);
        Category bottoms = save("하의", clothing, 2);

        Category electronics = save("전자기기", null, 2);
        Category laptops = save("노트북", electronics, 1);
        Category accessories = save("액세서리", electronics, 2);

        Category food = save("식품", null, 3);
        Category snacks = save("간식", food, 1);
        Category drinks = save("음료", food, 2);

        product(tops, "기본 반팔 티셔츠", 19000, 100);
        product(tops, "오버핏 후드티", 49000, 50);
        product(tops, "옥스포드 셔츠", 39000, 80);
        product(tops, "니트 스웨터", 59000, 30);
        product(bottoms, "와이드 데님 팬츠", 45000, 60);
        product(bottoms, "슬랙스", 39000, 70);
        product(bottoms, "조거 팬츠", 35000, 90);

        product(laptops, "울트라북 14", 1290000, 10);
        product(laptops, "게이밍 노트북 16", 1890000, 5);
        product(accessories, "무선 마우스", 29000, 200);
        product(accessories, "기계식 키보드", 89000, 40);
        product(accessories, "USB-C 허브", 45000, 120);
        product(accessories, "노트북 파우치", 25000, 150);

        product(snacks, "수제 쿠키 세트", 15000, 100);
        product(snacks, "견과류 믹스", 12000, 300);
        product(snacks, "다크 초콜릿", 9000, 0); // 품절 상품 예시
        product(drinks, "콜드브루 원액", 18000, 80);
        product(drinks, "녹차 티백", 8000, 250);

        log.info("로컬 샘플 데이터 생성 완료: 카테고리 {}개, 상품 {}개",
                categoryRepository.count(), productRepository.count());
    }

    private Category save(String name, Category parent, int sortOrder) {
        return categoryRepository.save(Category.create(name, parent, sortOrder));
    }

    private void product(Category category, String name, int price, int stock) {
        String imageUrl = "https://picsum.photos/seed/shoplab-" + (++imageSeq) + "/400/400";
        productRepository.save(Product.create(category, name, name + " 상품 설명입니다.", price, stock, imageUrl));
    }
}