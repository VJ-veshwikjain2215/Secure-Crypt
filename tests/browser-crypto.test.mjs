import {test} from 'node:test';
import assert from 'node:assert/strict';
import {encryptLocal,decryptLocal,inspectEnvelope} from '../src/main/resources/static/js/crypto.mjs';
test('browser AES is local and preserves large Unicode messages',async()=>{
 const previous=globalThis.fetch;globalThis.fetch=()=>{throw new Error('Unexpected network request');};
 try{const text='漢'.repeat(20000),password='नमस्ते-password';const encrypted=await encryptLocal(text,password);assert.equal(await decryptLocal(encrypted,password),text);assert.ok(encrypted.startsWith('SC1.600000.'));assert.notEqual(encrypted,await encryptLocal(text,password));}finally{globalThis.fetch=previous;}
});
test('browser rejects altered salt, nonce, ciphertext, header, wrong password, and truncation',async()=>{
 const value=await encryptLocal('Integrity matters','test-password');for(let i=0;i<5;i++){const f=value.split('.');f[i]=f[i][0]==='A'?'B'+f[i].slice(1):'A'+f[i].slice(1);await assert.rejects(decryptLocal(f.join('.'),'test-password'));}await assert.rejects(decryptLocal(value,'wrong'));await assert.rejects(decryptLocal(value.slice(0,-3),'test-password'));assert.throws(()=>inspectEnvelope(value+'='));
});
test('limits enforced before expensive work',async()=>{await assert.rejects(encryptLocal('A'.repeat(20001),'key'));await assert.rejects(encryptLocal('text','A'.repeat(1025)));});
import {pbkdf2Sync,createCipheriv} from 'node:crypto';
import {decryptLegacyLocal} from '../src/main/resources/static/js/crypto.mjs';
test('historical AES-CBC remains explicitly decryptable',async()=>{const password='Legacy-test',iv=Buffer.alloc(16,7),key=pbkdf2Sync(password,'SecureCrypt$Salt',65536,16,'sha256'),cipher=createCipheriv('aes-128-cbc',key,iv);const text='Old message नमस्ते';const encrypted=Buffer.concat([iv,cipher.update(text,'utf8'),cipher.final()]).toString('base64');assert.equal(await decryptLegacyLocal(encrypted,password),text);});
