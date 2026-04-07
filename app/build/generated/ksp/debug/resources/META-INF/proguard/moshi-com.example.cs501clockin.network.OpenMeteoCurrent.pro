-if class com.example.cs501clockin.network.OpenMeteoCurrent
-keepnames class com.example.cs501clockin.network.OpenMeteoCurrent
-if class com.example.cs501clockin.network.OpenMeteoCurrent
-keep class com.example.cs501clockin.network.OpenMeteoCurrentJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
