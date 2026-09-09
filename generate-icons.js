const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

const sizes = [
  { dir: 'mipmap-mdpi', size: 48 },
  { dir: 'mipmap-hdpi', size: 72 },
  { dir: 'mipmap-xhdpi', size: 96 },
  { dir: 'mipmap-xxhdpi', size: 144 },
  { dir: 'mipmap-xxxhdpi', size: 192 },
];

const svgPath = path.join(__dirname, 'assets', 'icon.svg');
const resPath = path.join(__dirname, 'android', 'app', 'src', 'main', 'res');

async function generateIcons() {
  const svgBuffer = fs.readFileSync(svgPath);

  for (const { dir, size } of sizes) {
    const dirPath = path.join(resPath, dir);
    if (!fs.existsSync(dirPath)) {
      fs.mkdirSync(dirPath, { recursive: true });
    }

    await sharp(svgBuffer)
      .resize(size, size)
      .png()
      .toFile(path.join(dirPath, 'ic_launcher.png'));

    await sharp(svgBuffer)
      .resize(size, size)
      .png()
      .toFile(path.join(dirPath, 'ic_launcher_round.png'));

    console.log(`✓ ${dir} (${size}x${size})`);
  }

  console.log('\nIconos generados correctamente.');
}

generateIcons().catch(console.error);
