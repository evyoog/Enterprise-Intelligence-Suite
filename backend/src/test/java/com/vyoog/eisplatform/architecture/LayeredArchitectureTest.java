package com.vyoog.eisplatform.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Enforces the module layering documented in the folder structure: controller ->
 * service -> repository, with no layer reaching back up or sideways into another
 * module's internals.
 */
class LayeredArchitectureTest {

    private static final String BASE_PACKAGE = "com.vyoog.eisplatform";

    @Test
    void controllersShouldOnlyBeAccessedByOtherControllersOrConfig() {
        ArchRule rule = classes().that().resideInAPackage("..controller..")
            .should().onlyBeAccessed().byAnyPackage("..controller..", "..config..");

        rule.check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }

    @Test
    void repositoriesShouldOnlyBeAccessedByServicesOrOtherRepositories() {
        ArchRule rule = classes().that().resideInAPackage("..repository..")
            .should().onlyBeAccessed().byAnyPackage("..repository..", "..service..", "..config..");

        rule.check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }

    @Test
    void modulesShouldNotDependOnEachOthersInternals() {
        // Both extra packages here are deliberate, not holes in this rule:
        // - ..modules.platform.. already reused ProductImageService for its own
        //   logo upload/serving before this test was last run clean (found live
        //   — this rule was already failing for that reason, independent of
        //   anything below).
        // - ..modules.registration.. reuses the EXISTING product catalog
        //   (ProductRepository/ProductService/Product) on purpose, per its own
        //   explicit requirement to never duplicate the catalog just because a
        //   new module needed to read it.
        // - ..modules.dashboard.. (Phase 16) reuses ProductRepository the same
        //   way, only to confirm a product id is real before favoriting/
        //   recording a launch against it — not a duplicate catalog either.
        // - ..modules.servicestatus.. (REQ-PRT-001, 2026-09-26) reads the same
        //   catalog to list the products whose status is posted — again no
        //   duplicate catalog.
        // - ..modules.administration.. (15.01 Platform Administration, sprint
        //   2026.4.2) seeds PlatformCurrency rows from the existing Currency
        //   enum (PlatformAdministrationSeeder) — reusing the fixed currency
        //   set, not defining a second one.
        ArchRule rule = classes().that().resideInAPackage("..modules.product..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(
                "..modules.product..", "..modules.platform..", "..modules.registration..", "..modules.dashboard..",
                "..modules.servicestatus..", "..modules.administration..",
                "com.vyoog.eisplatform", "..config.."
            );

        rule.check(new ClassFileImporter().importPackages(BASE_PACKAGE));
    }
}
