/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataServiceImpl - TeaVM implementation of DataScene.DataService
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.DataScene;

/**
 * TeaVM implementation of DataScene.DataService.
 * Bridges DataScene UI with WebDataManager.
 */
public class WebDataServiceImpl implements DataScene.DataService {

    // Total counts (must match DataScene calculations)
    private static final int TOTAL_BADGES = 192; // Badge enum count approx
    private static final int TOTAL_CATALOG = 400; // Max catalog items
    private static final int TOTAL_BESTIARY = 200; // Max bestiary entries
    private static final int TOTAL_LORE = 30; // 5 lore docs * 6 pages
    private static final int TOTAL_GUIDE = 14; // Adventurer's guide pages
    private static final int TOTAL_ALCHEMY = 9; // Alchemy guide pages

    @Override
    public String exportData() {
        WebDataManager.ExportResult result = WebDataManager.exportData();
        if (result.success) {
            return result.url;
        }
        return null;
    }

    @Override
    public boolean copyToClipboard(String url) {
        return WebDataManager.copyToClipboard(url);
    }

    @Override
    public boolean hasImportData() {
        return WebDataManager.hasImportData();
    }

    @Override
    public String getImportPreview(String url) {
        WebDataManager.ImportPreview preview = WebDataManager.previewImport(url);
        if (!preview.valid) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        // Use Messages.get for proper localization, same format as DataScene
        // 1. Rankings
        sb.append(Messages.get(DataScene.class, "rankings", preview.rankingCount, preview.highestScore));
        sb.append("\n");

        // 2. Badges
        sb.append(Messages.get(DataScene.class, "badges", preview.badgeCount, TOTAL_BADGES));
        if (preview.newBadges > 0) {
            sb.append(" (+").append(preview.newBadges).append(")");
        }
        sb.append("\n");

        // 3. Catalog
        sb.append(Messages.get(DataScene.class, "catalog", preview.catalogCount, TOTAL_CATALOG));
        if (preview.newCatalogItems > 0) {
            sb.append(" (+").append(preview.newCatalogItems).append(")");
        }
        sb.append("\n");

        // 4. Bestiary
        sb.append(Messages.get(DataScene.class, "bestiary", preview.bestiaryCount, TOTAL_BESTIARY));
        if (preview.newBestiaryEntries > 0) {
            sb.append(" (+").append(preview.newBestiaryEntries).append(")");
        }
        sb.append("\n");

        // 5. Lore
        sb.append(Messages.get(DataScene.class, "lore", preview.loreCount, TOTAL_LORE));
        if (preview.newLorePages > 0) {
            sb.append(" (+").append(preview.newLorePages).append(")");
        }
        sb.append("\n");

        // 6. Guide
        sb.append(Messages.get(DataScene.class, "guide", preview.guideCount, TOTAL_GUIDE));
        if (preview.newGuidePages > 0) {
            sb.append(" (+").append(preview.newGuidePages).append(")");
        }
        sb.append("\n");

        // 7. Alchemy
        sb.append(Messages.get(DataScene.class, "alchemy", preview.alchemyCount, TOTAL_ALCHEMY));
        if (preview.newAlchemyPages > 0) {
            sb.append(" (+").append(preview.newAlchemyPages).append(")");
        }

        return sb.toString();
    }

    @Override
    public boolean applyImport(String url, boolean overwrite) {
        WebDataManager.ImportPreview preview = WebDataManager.previewImport(url);
        if (!preview.valid) {
            return false;
        }

        WebDataMerger.MergePolicy policy = overwrite
                ? WebDataMerger.MergePolicy.OVERWRITE
                : WebDataMerger.MergePolicy.MERGE_UNION;

        WebDataMerger.MergeResult result = WebDataManager.applyImport(preview, policy);
        return result.hasChanges() || overwrite;
    }

    @Override
    public String getUrlFragment() {
        return WebUrlCodec.getUrlFragment();
    }

    @Override
    public void clearUrlFragment() {
        WebUrlCodec.clearDataFragment();
    }

    @Override
    public void reloadPage() {
        WebUrlCodec.reloadPage();
    }

    @Override
    public boolean hasSeedParams() {
        return WebUrlCodec.hasSeedParams();
    }

    @Override
    public String getSeedParam() {
        return WebUrlCodec.getSeedParam();
    }

    @Override
    public String getClassParam() {
        return WebUrlCodec.getClassParam();
    }

    @Override
    public int getChallengesParam() {
        return WebUrlCodec.getChallengesParam();
    }

    @Override
    public void clearSeedParams() {
        WebUrlCodec.clearSeedParams();
    }

    @Override
    public void clearAllBrowserData() {
        WebDataManager.clearAllData();
    }
}
