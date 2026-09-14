package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.entity.Asset;
import com.whereismymoney.backend.repository.AssetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository assetRepository;

    public AssetController(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @GetMapping("/{id}")
    public Asset getAsset(@PathVariable Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    @GetMapping("/symbol/{symbol}")
    public Asset getBySymbol(@PathVariable String symbol) {
        return assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asset createAsset(@RequestBody Asset asset) {
        return assetRepository.save(asset);
    }

    @PutMapping("/{id}")
    public Asset updateAsset(
            @PathVariable Long id,
            @RequestBody Asset updatedAsset) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        asset.setSymbol(updatedAsset.getSymbol());
        asset.setName(updatedAsset.getName());
        asset.setAssetType(updatedAsset.getAssetType());
        asset.setCurrency(updatedAsset.getCurrency());

        return assetRepository.save(asset);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable Long id) {
        assetRepository.deleteById(id);
    }
}