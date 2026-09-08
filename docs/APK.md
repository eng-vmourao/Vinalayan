# APK do Vinalayan

O Vinalayan usa a distribuição `localRelease` do OpenDash como base: APKs assinados para ARM64, ARM de 32 bits, x86, x86_64 e um APK universal.

## Instalação no celular

1. Entre no GitHub com a conta que tem acesso ao repositório privado.
2. Abra [Releases](https://github.com/eng-vmourao/Vinalayan/releases).
3. Baixe `Vinalayan-0.1.2-preview-universal.apk`. Se souber que seu celular é ARM64, a variante `arm64-v8a` ocupa menos espaço.
4. Abra o arquivo no Android, permita a instalação por esse navegador/gerenciador quando solicitado e toque em **Instalar**.
5. Abra **Vinalayan** e toque em **Continue** para usar os dados locais.

Depois de instalar a versão 0.1.2 ou posterior, use **More → Update from GitHub → Check**. O aplicativo consulta as versões públicas, baixa o APK universal mais recente, valida o arquivo e abre o instalador do Android. Na primeira atualização, autorize o Vinalayan como fonte de instalação. O Android sempre exige sua confirmação final no botão **Instalar**.

Requer Android 7.0 ou posterior. O pacote `com.vinalayan.app` permite instalar Vinalayan junto com OpenDash. Os dados dos dois aplicativos são separados. Esta é uma prévia; a validação no seu celular ainda é necessária.

## Geração pelo GitHub

O workflow **Build Vinalayan APK** executa automaticamente quando o código de compilação/aplicativo muda na `main`; também pode ser iniciado em **Actions → Build Vinalayan APK → Run workflow**.

O workflow configura Java 21 e Android SDK, executa testes unitários e Android Lint, compila `localRelease`, verifica assinatura, alinhamento e identidade dos APKs e disponibiliza o artefato **Vinalayan-APK** por 30 dias. O código de versão aumenta com o número da execução para permitir atualização no Android.

Depois de uma compilação bem-sucedida, **Test Vinalayan APK on Android** instala o APK universal em um emulador Android 15 e verifica a abertura e as quatro abas. Capturas e logs ficam no artefato **Vinalayan-Android-smoke**. Esse teste não substitui uso real, GPS ou validação em uma moto.

As Releases são publicadas após a revisão dos resultados. Artefatos do Actions e anexos de Releases são formas distintas de baixar o APK.

## Assinatura e atualização

Os secrets `VINALAYAN_KEYSTORE_BASE64` e `VINALAYAN_KEYSTORE_PASSWORD` guardam a chave PKCS12 e sua senha no GitHub. O alias é `vinalayan`. A chave não é incluída no repositório nem nos artefatos.

A cópia local fica em `%LOCALAPPDATA%\Vinalayan\signing\vinalayan-release.p12`. A senha local, em `password.dpapi`, é protegida pelo Windows para o usuário que a criou. Preserve a chave e uma cópia recuperável da senha para continuar assinando atualizações, especialmente antes de trocar de computador ou reinstalar o Windows.

O workflow fornece as propriedades `OPENDASH_RELEASE_*` já existentes no projeto para assinar a variante. Esses nomes internos foram mantidos por compatibilidade com a configuração original.

## Serviços opcionais

Esta compilação funciona com armazenamento local, sem configuração Firebase e sem chave Mapbox. A visualização de mapas/rotas usa os provedores abertos já presentes no projeto e precisa de internet. Sincronização Google/Firebase e Mapbox exigem configuração própria. A integração com o painel da moto foi retirada pelo projeto original e não faz parte deste APK.
