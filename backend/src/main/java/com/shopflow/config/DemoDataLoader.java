package com.shopflow.config;

import com.shopflow.domain.*;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DemoDataLoader {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;
    @Bean
    CommandLineRunner loadDemo() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
                userRepository.save(User.builder()
                    .email("admin@shopflow.com")
                    .passwordHash(passwordEncoder.encode("Admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .active(true)
                    .build());

            var seller = userRepository.save(User.builder()
                    .email("seller@shopflow.com")
                    .passwordHash(passwordEncoder.encode("Seller123"))
                    .firstName("Sam")
                    .lastName("Seller")
                    .role(Role.SELLER)
                    .active(true)
                    .build());
            var sp = SellerProfile.builder()
                    .user(seller)
                    .shopName("Souk Lahad")
                    .description("Maybe Used electronics and accessories")
                    .logoUrl("https://picsum.photos/seed/shop/120/120")
                    .build();
            seller.setSellerProfile(sp);
            userRepository.save(seller);

            var customer = userRepository.save(User.builder()
                    .email("customer@shopflow.com")
                    .passwordHash(passwordEncoder.encode("Customer123"))
                    .firstName("Amine")
                    .lastName("Customer")
                    .role(Role.CUSTOMER)
                    .active(true)
                    .build());

            address(customer, "10 Rue Maghreb", "Ouardanine", "5010", "TN", true);

            var catElectronics = category("Electronics", "Devices and more", null);
            var catFashion = category("Fashion", "Wearables", null);
            var catPhones = category("Phones", "Smartphones", catElectronics);

            product(seller, "Wireless Headphones", "Noise cancelling, 30h battery", new BigDecimal("89.99"),
                    new BigDecimal("69.99"), 40,
                    new Category[]{catElectronics, catPhones},
                    "https://images.unsplash.com/photo-1657223144998-e5aa4fa2db7c?q=80&w=765&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                    variant("Color", "Black", 10, BigDecimal.ZERO),
                    variant("Color", "White", 10, new BigDecimal("5.00")));

            product(seller, "USB-C Hub", "7-in-1 adapter", new BigDecimal("45.00"), null, 100, catElectronics,
                    "https://images.unsplash.com/photo-1760376789487-994070337c76?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");

            product(seller, "Cotton T-Shirt", "Soft organic cotton", new BigDecimal("24.99"), new BigDecimal("19.99"),
                    200, catFashion, "https://images.unsplash.com/photo-1621951753015-740c699ab970?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                    variant("Size", "S", 20, BigDecimal.ZERO),
                    variant("Size", "M", 30, BigDecimal.ZERO),
                    variant("Size", "L", 25, BigDecimal.ZERO));

            product(seller, "MackBook", "14-inch laptop, 16GB RAM, 512GB SSD",
                    new BigDecimal("1199.00"), new BigDecimal("1099.00"), 15, catElectronics,
                    "https://images.unsplash.com/photo-1627513074408-192f520f2887?q=80&w=735&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");

            product(seller, "Winter Coat", "Warm and water-resistant winter coat",
                    new BigDecimal("129.00"), null, 50, catFashion,
                    "https://images.unsplash.com/photo-1674471361345-e787eaf7dd8c?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                    variant("Size", "M", 10, BigDecimal.ZERO),
                    variant("Size", "L", 8, BigDecimal.ZERO));

            product(seller, "Hat", "Fits for all head shapes",
                    new BigDecimal("14.90"), new BigDecimal("10.90"), 80, catFashion,
                    "https://images.unsplash.com/photo-1521369909029-2afed882baee?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");

            couponRepository.save(Coupon.builder()
                    .code("SAVE10")
                    .type(CouponType.PERCENT)
                    .value(new BigDecimal("10"))
                    .active(true)
                    .maxUses(1000)
                    .currentUses(0)
                    .build());

            couponRepository.save(Coupon.builder()
                    .code("FLAT5")
                    .type(CouponType.FIXED)
                    .value(new BigDecimal("5.00"))
                    .active(true)
                    .maxUses(500)
                    .currentUses(0)
                    .build());

            couponRepository.save(Coupon.builder()
                    .code("TESTSHOP")
                    .type(CouponType.PERCENT)
                    .value(new BigDecimal("15"))
                    .active(true)
                    .maxUses(10000)
                    .currentUses(0)
                    .build());
        };
    }

    private void address(User u, String street, String city, String pc, String country, boolean principal) {
        var a = Address.builder()
                .user(u)
                .street(street)
                .city(city)
                .postalCode(pc)
                .country(country)
                .principal(principal)
                .build();
        u.getAddresses().add(a);
        userRepository.save(u);
    }

    private Category category(String name, String desc, Category parent) {
        var c = Category.builder().name(name).description(desc).parent(parent).build();
        return categoryRepository.save(c);
    }

    private void product(User seller, String name, String desc, BigDecimal price, BigDecimal promo, int stock,
                         Category mainCat, String image, ProductVariant... variants) {
        product(seller, name, desc, price, promo, stock, new Category[]{mainCat}, image, variants);
    }

    private void product(User seller, String name, String desc, BigDecimal price, BigDecimal promo, int stock,
                         Category[] cats, String image, ProductVariant... variants) {
        var p = Product.builder()
                .seller(seller)
                .name(name)
                .description(desc)
                .price(price)
                .promoPrice(promo)
                .stock(stock)
                .active(true)
                .build();
        for (var c : cats) {
            p.getCategories().add(c);
        }
        p.getImageUrls().add(image);
        for (var template : variants) {
            var v = ProductVariant.builder()
                    .product(p)
                    .attributeName(template.getAttributeName())
                    .attributeValue(template.getAttributeValue())
                    .additionalStock(template.getAdditionalStock())
                    .priceDelta(template.getPriceDelta())
                    .build();
            p.getVariants().add(v);
        }
        productRepository.save(p);
    }

    private ProductVariant variant(String attr, String val, int addStock, BigDecimal delta) {
        return ProductVariant.builder()
                .attributeName(attr)
                .attributeValue(val)
                .additionalStock(addStock)
                .priceDelta(delta)
                .build();
    }
}
