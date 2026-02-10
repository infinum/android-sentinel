import com.infinum.maven.SonatypeConfiguration

extra["sonatype"] = SonatypeConfiguration().apply {
    load()
}
