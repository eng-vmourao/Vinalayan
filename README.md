# Vinalayan

Aplicativo Android pessoal de Vinícius para sua Himalayan, baseado no [OpenDash](https://github.com/subtlesayak/open-dash). Reúne veículos, manutenção, abastecimentos, despesas e visualização de rotas.

Esta versão adota o nome Vinalayan. O histórico, a licença e os créditos do projeto original estão preservados. A [prévia 0.1.2 para Android](https://github.com/eng-vmourao/Vinalayan/releases/tag/v0.1.2-preview) está disponível para instalação, com o Real Brasileiro como moeda padrão e atualização pelo próprio aplicativo.

## Upstream project notice

> [!WARNING]
> Royal Enfield contacted the OpenDash project, and after discussions the project is removing dash connection/projection protocols, proprietary code, and dash wallpaper functionality from future releases. OpenDash is being refocused as a clean, independent app around route preview, vehicle management, maintenance, garage, expenses, and downloadable wallpapers. Existing dash-related builds may continue to work only while the dash still allows them, but dash connection issues will not be fixed going forward.

## Overview

Vinalayan is an open-source Android app for motorcycle ownership, trip prep, and ride-adjacent tools, based on OpenDash. The project is moving forward without dash connection, projection, reverse-engineered protocol, or proprietary integration code.

The new direction is simple: keep the useful rider tools, make the app clean and independent, and rebuild only around original app-only features.

## Current Focus

- Route preview from shared map links or `geo:` links.
- Vehicle profiles with odometer, PUC, insurance, and service details.
- Garage and maintenance tracking for parts, service intervals, and service history.
- Expense tracking for fuel, repairs, accessories, riding gear, food, stays, transport, and other ownership costs.
- Downloadable wallpaper pack in Settings.
- Material 3 UI themes.
- Local-first storage, with optional bring-your-own Firebase/Google sync where configured.

## Removed Direction

Future Vinalayan releases are not intended to include:

- Dash pairing or connection flows.
- Dash projection, video streaming, media/call cards, or hardware control.
- Reverse-engineered dash protocol/session/auth code.
- Dash wallpaper upload or playback features.
- Bug fixes for dash connection behavior in older builds.

## Install

Baixe **Vinalayan-0.1.2-preview-universal.apk** na [página da prévia](https://github.com/eng-vmourao/Vinalayan/releases/tag/v0.1.2-preview), abra o arquivo no Android e permita a instalação quando solicitado. Nas próximas versões, use **More → Update from GitHub → Check** para baixar e validar a atualização pelo aplicativo. Requer Android 7.0 ou posterior.

A prévia passou em 12 testes unitários, Android Lint sem erros, verificações de assinatura e instalação/abertura das quatro abas em emulador Android 15. Ainda precisa ser validada no seu celular. Veja o [guia completo de instalação e atualização](docs/APK.md).

## First Use

1. Open Vinalayan.
2. Add your motorcycle in **Vehicles**.
3. Add odometer, PUC, insurance, and service details.
4. Log fuel, maintenance, and ownership costs in **Garage** and **Expenses**.
5. Share a destination or `geo:` link into Vinalayan to preview a route.
6. Use **More** for account, sync, appearance, map provider, and wallpaper downloads.

## Main Tabs

| Tab | What it does |
| --- | --- |
| Vehicles | Add/edit vehicles and choose the active vehicle |
| Expenses | Add, filter, review, and export expenses |
| Garage | Odometer, mileage, spare parts, and service logging |
| More | Account, sync, themes, navigation provider, units, help, and wallpaper downloads |

Route preview opens from shared destinations and saved locations instead of being a permanent bottom tab.

## Build From Source

```bash
git clone https://github.com/eng-vmourao/Vinalayan.git
cd Vinalayan
./gradlew :app:assembleLocalDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleLocalDebug
```

Run local unit tests:

```bash
./gradlew :app:testLocalDebugUnitTest
```

The [APK guide](docs/APK.md) explains GitHub Actions builds, installation, signing, and updates. Release signing uses your own keystore through Gradle properties or CI secrets. Never commit keys, APKs, logs, `local.properties`, `key.properties`, `google-services.json`, keystores, tokens, or other private files.

## Release Variants

- `localRelease` builds signed APKs for GitHub releases with application id `com.vinalayan.app`.
- `playRelease` retains the optional Google Play bundle configuration with application id `com.vinalayan.app`; it requires its own service configuration before publication.
- The preview APK uses local storage and open map/route providers. Firebase sync and Mapbox are optional and require your own configuration.

## Privacy

- App data is local-first.
- Expense exports are created locally and shared only when you choose to share them.
- Firebase/Google sync is optional and bring-your-own-project.
- Release builds should avoid logging full URLs, coordinates, account IDs, or device identifiers.
- The app should not collect dash credentials or connect to motorcycle dash hardware going forward.

## Contributing

Issues and pull requests are welcome for the app-only direction: route preview, vehicles, garage, maintenance, expenses, sync, themes, and downloadable wallpapers.

Please remove personal data from logs and screenshots before sharing: coordinates, SSIDs, account IDs, tokens, and device identifiers.

## License

Vinalayan is based on OpenDash and distributed under the terms in [`LICENSE`](LICENSE). Original attribution is preserved in [`NOTICE`](NOTICE).

## References

- [norbertFeron/better-dash](https://github.com/norbertFeron/better-dash) - Early motivation
- [adityadasika21/NorthStar](https://github.com/adityadasika21/NorthStar) - Original app base
