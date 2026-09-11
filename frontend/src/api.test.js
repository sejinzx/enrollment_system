import test from 'node:test';
import assert from 'node:assert/strict';
import {decodeUser, request} from './api.js';
test('JWT handles Korean usernames, roles, malformed and expired tokens',()=>{
 const token=data=>`header.${Buffer.from(JSON.stringify(data)).toString('base64url')}.sig`;
 assert.deepEqual(decodeUser(token({userId:'사용자',role:'ROLE_CREATOR',exp:Date.now()/1000+300})),{userId:'사용자',role:'CREATOR'});
 assert.equal(decodeUser(token({exp:1})),null);
 assert.equal(decodeUser('bad'),null);
});
test('API preserves accepted enrollment responses and bearer authorization',async()=>{
 const original=global.fetch;
 try { global.fetch=async(url,options)=>{assert.equal(url,'/api/enrollments/classes/7');assert.equal(options.method,'POST');assert.equal(options.headers.Authorization,'Bearer token');return new Response(JSON.stringify({classSeq:7,message:'수강 신청 요청 접수'}),{status:202});};
 assert.equal((await request('/enrollments/classes/7',{token:'token',method:'POST'})).message,'수강 신청 요청 접수');
 } finally {global.fetch=original;}
});
test('API propagates backend business errors and session expiry',async()=>{
 const original=global.fetch;
 try {global.fetch=async()=>new Response(JSON.stringify({error:'정원이 초과되었습니다'}),{status:409});await assert.rejects(request('/classes'),{message:'정원이 초과되었습니다',status:409});
 global.fetch=async()=>new Response('',{status:401});await assert.rejects(request('/classes'),{status:401});
 } finally {global.fetch=original;}
});

test('logout sends bearer token and accepts an empty success response',async()=>{
 const original=global.fetch;
 try {
  global.fetch=async(url,options)=>{
   assert.equal(url,'/api/users/logout');
   assert.equal(options.method,'POST');
   assert.equal(options.headers.Authorization,'Bearer session-token');
   assert.equal(options.body,undefined);
   return new Response(null,{status:200});
  };
  assert.deepEqual(await request('/users/logout',{token:'session-token',method:'POST'}),{});
 } finally {global.fetch=original;}
});
test('logout exposes server failures for retry',async()=>{
 const original=global.fetch;
 try {
  global.fetch=async()=>new Response(null,{status:500});
  await assert.rejects(request('/users/logout',{token:'session-token',method:'POST'}),{status:500});
 } finally {global.fetch=original;}
});
