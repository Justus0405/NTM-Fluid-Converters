# Building from Source

## Supported HBM forks

| Mod              | GitHub                                              | Jar naming pattern |
| ---------------- | --------------------------------------------------- | ------------------ |
| Original HBM NTM | <https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT> | `HBM-NTM-*.jar`    |
| JameH2 fork      | <https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT>  | `*_H*.jar`         |

## Build Dependencies

The `libs/` folder isn't committed to the repo. Before building, grab a release jar from one of the forks above and drop it in `libs/`.

> [!IMPORTANT]
> Without a JAR in `libs/` the build will fail with a clear error message.
> With more than one JAR in `libs/` the build will also fail,keep only one jar there at a time.

## Build steps

1. Clone the repository:

```shell
git clone https://github.com/Justus0405/NTM-Fluid-Converters.git
```

2. Navigate to the directory:

```shell
cd NTM-Fluid-Converters
```

3. Place one HBM jar inside libs/ (original or JameH2 fork, any version):

```shell
cp /path/to/HBM-NTM-*.jar libs/
```

4. Build with Gradle:

```shell
./gradlew build
```

> [!TIP]
> The JAR is written to: `build/libs/ntmfluidconverters-1.0.jar`
