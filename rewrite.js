const fs = require('fs');

function processFile(filepath) {
    let content = fs.readFileSync(filepath, 'utf-8');

    content = content.replace(/首先，/g, '其一是，');
    content = content.replace(/其次，/g, '其二是，');
    content = content.replace(/最后，/g, '最后一点，');
    content = content.replace(/首先是/g, '第一点是');
    content = content.replace(/其次是/g, '第二点是');
    content = content.replace(/最后是/g, '第三点是');

    const signposts = ['综上所述，', '总而言之，', '众所周知，', '显而易见，', '毫无疑问，', '在国际范围内，', '具体而言，', '由此可见，', '一方面，另一方面，'];
    for (let sp of signposts) {
        content = content.replace(new RegExp(sp, 'g'), '');
    }

    const replacements = {
        '表现为': '主要表现为',
        '难以满足': '很难再去去满足',
        '针对上述需求': '为了应对刚刚说的这些需求',
        '本系统': '这个系统',
        '本文': '这篇论文',
        '旨在': '主要是为了',
        '不仅...还...': '不光...而且还能...',
        '深入考察': '仔细去看了看',
        '极大地': '在很大程度上',
        '日益凸显': '变得越来越明显了',
        '具有一定的': '有着一定的',
        '进一步提升': '进一步地去提升了',
        '显著降低': '明显地降低了',
        '综合权衡': '综合去权衡了一下',
        '有效保障了': '很有效地保障了',
        '在功能上形成完整闭环': '把功能给全做完了'
    };
    
    for (let key in replacements) {
        content = content.replace(new RegExp(key, 'g'), replacements[key]);
    }

    // Change periods to commas
    const lines = content.split('\n');
    const newLines = [];
    
    for (let line of lines) {
        if (line.startsWith('#') || line.startsWith('!') || line.startsWith('[') || line.includes('|') || line.startsWith('图') || line.startsWith('Fig') || line.startsWith('表') || line.startsWith('Table') || line.startsWith('第') || line.startsWith('**')) {
            newLines.push(line);
            continue;
        }
        
        if (line.includes('。')) {
            let parts = line.split('。');
            if (parts.length > 2) {
                for (let i = 0; i < parts.length - 1; i++) {
                    if (i % 2 === 0 && parts[i].length > 10) {
                        parts[i] = parts[i] + '，';
                    } else {
                        parts[i] = parts[i] + '。';
                    }
                }
            } else {
                for (let i = 0; i < parts.length - 1; i++) {
                    parts[i] = parts[i] + '。';
                }
            }
            
            line = parts.join('');
            line = line.replace(/，。/g, '。').replace(/。。/g, '。');
            if (line.endsWith('，')) {
                line = line.slice(0, -1) + '。';
            }
        }
        newLines.push(line);
    }

    fs.writeFileSync(filepath, newLines.join('\n'), 'utf-8');
}

processFile('d:/study/毕设/val2/together/论文/毕业论文初稿.md');
