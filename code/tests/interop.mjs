import assert from 'node:assert/strict';
import {encryptLocal,decryptLocal} from '../src/main/resources/static/js/crypto.mjs';
const base=process.env.BASE_URL || 'http://localhost:5181';
async function api(operation,text,key){const response=await fetch(base+'/api/'+operation,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({algorithm:'AES',text,key})});const body=await response.json();assert.equal(response.status,200,body.message);return body.result;}
for(const [text,key] of [['Hello नमस्ते 🔐 é','नमस्ते-password'],['漢'.repeat(20000),'Boundary-test-password']]){
 assert.equal(await api('decrypt',await encryptLocal(text,key),key),text);
 assert.equal(await decryptLocal(await api('encrypt',text,key),key),text);
}
console.log('PASS: browser / Java compatibility: 4 Unicode and maximum-length round trips.');
