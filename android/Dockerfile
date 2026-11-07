FROM ubuntu:22.04

# Install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
RUN mkdir -p $ANDROID_HOME/cmdline-tools
WORKDIR $ANDROID_HOME

# Download and setup SDK tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip && \
    unzip -q commandlinetools-linux-10406996_latest.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/bin cmdline-tools/lib cmdline-tools/latest/ && \
    rm commandlinetools-linux-10406996_latest.zip

ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses && \
    sdkmanager --install \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "platform-tools" \
    "tools"

# Setup workspace
WORKDIR /workspace

# Copy gradle wrapper first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./

# Download dependencies
RUN chmod +x gradlew && ./gradlew --version

# Copy rest of project
COPY app/ app/

# Build APK
RUN ./gradlew clean assembleRelease --no-daemon --stacktrace

# Copy APK to output
RUN mkdir -p /output && \
    cp app/build/outputs/apk/release/app-release.apk /output/ || \
    (echo "APK not found, listing build outputs:" && find app/build -name "*.apk" && exit 1)

CMD ["/bin/bash"]
