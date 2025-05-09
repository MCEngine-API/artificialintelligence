# --- Keep class and selected public methods for MCEngineArtificialIntelligenceApi ---
-keep class io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi {
    public static io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi getApi();
    public org.bukkit.plugin.Plugin getPlugin();
    public java.lang.String getPlayerToken(java.lang.String, java.lang.String);
    public void registerModel(java.lang.String, java.lang.String);
    public io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel getAi(java.lang.String, java.lang.String);
    public java.lang.String getResponse(java.lang.String, java.lang.String, java.lang.String);
    public java.lang.String getResponse(java.lang.String, java.lang.String, java.lang.String, java.lang.String);
}

# --- Keep these classes and all their members ---
-keep class io.github.mcengine.api.artificialintelligence.addon.IMCEngineArtificialIntelligenceAddOn { *; }
-keep class io.github.mcengine.api.artificialintelligence.addon.MCEngineArtificialIntelligenceAddOnLogger { *; }
-keep class io.github.mcengine.api.artificialintelligence.dlc.IMCEngineArtificialIntelligenceDLC { *; }
-keep class io.github.mcengine.api.artificialintelligence.dlc.MCEngineArtificialIntelligenceDLCLogger { *; }

# --- Keep plugin entry methods like onLoad ---
-keepclassmembers class * {
    public void onLoad(...);
}

-dontoptimize    # optional: remove if you want bytecode optimization
-dontwarn        # optional: silences warnings
-dontnote        # optional: silences notes
-dontusemixedcaseclassnames
-overloadaggressively