import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import adminService, { UserDTO, QuoteRequestAdminDTO, UserSceneDTO } from '../../controllers/api/admin/adminService';
import './AdminDashboard.css';
import authService from '../../controllers/api/auth/authService';
import { ProductService } from '../../controllers/api/products/ProductService';
import { CategoryService } from '../../controllers/api/products/CategoryService';
import { Product, Category } from '../../types/api';
import { HelpModal } from '../common';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const BACKEND_URL = API_BASE_URL.replace('/api', '');

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'users' | 'scenes' | 'requests' | 'products'>('requests');
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [scenes, setScenes] = useState<UserSceneDTO[]>([]);
  const [requests, setRequests] = useState<QuoteRequestAdminDTO[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedRequest, setSelectedRequest] = useState<QuoteRequestAdminDTO | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [showProductModal, setShowProductModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

  // Form states
  const [statusUpdate, setStatusUpdate] = useState('');
  const [adminResponse, setAdminResponse] = useState('');
  const [uploadFile, setUploadFile] = useState<File | null>(null);

  // Product form states
  const [productForm, setProductForm] = useState({
    name: '',
    description: '',
    priceRange: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
    modelPath: '',
    thumbnail: '',
    mountingType: 'FLOOR' as 'FLOOR' | 'WALL' | 'FREESTANDING',
    categoryId: 0,
  });

  useEffect(() => {
    if (!authService.isAdmin()) {
      navigate('/');
      return;
    }
    loadData();
  }, [navigate, activeTab]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'users') {
        const data = await adminService.getAllUsers();
        setUsers(data);
      } else if (activeTab === 'scenes') {
        const data = await adminService.getAllScenes();
        setScenes(data);
      } else if (activeTab === 'requests') {
        const data = await adminService.getAllQuoteRequests();
        setRequests(data);
      } else if (activeTab === 'products') {
        const [productsData, categoriesData] = await Promise.all([
          ProductService.getAll(),
          CategoryService.getAll()
        ]);
        setProducts(productsData);
        setCategories(categoriesData);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectRequest = (request: QuoteRequestAdminDTO) => {
    setSelectedRequest(request);
    setStatusUpdate(request.status);
    setAdminResponse(request.adminResponse || '');
  };

  const handleUpdateRequest = async () => {
    if (!selectedRequest) return;

    setLoading(true);
    setError('');
    try {
      const updated = await adminService.updateQuoteRequest(selectedRequest.id, {
        status: statusUpdate,
        adminResponse: adminResponse,
      });
      
      // Update the request in the list
      setRequests(requests.map(r => r.id === updated.id ? updated : r));
      setSelectedRequest(updated);
      alert('Quote request updated successfully!');
    } catch (err: any) {
      setError(err.message || 'Failed to update request');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async () => {
    if (!selectedRequest || !uploadFile) return;

    setLoading(true);
    setError('');
    try {
      const updated = await adminService.uploadDocument(selectedRequest.id, uploadFile);
      setRequests(requests.map(r => r.id === updated.id ? updated : r));
      setSelectedRequest(updated);
      setUploadFile(null);
      alert('Document uploaded successfully!');
    } catch (err: any) {
      setError(err.message || 'Failed to upload document');
    } finally {
      setLoading(false);
    }
  };

  const renderUsersTab = () => (
    <div className="admin-table-container">
      <h2>User Management</h2>
      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Company</th>
            <th>Role</th>
            <th>Scenes</th>
            <th>Requests</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.firstName} {user.lastName}</td>
              <td>{user.email}</td>
              <td>{user.company || '-'}</td>
              <td><span className={`role-badge ${user.role.toLowerCase()}`}>{user.role}</span></td>
              <td>{user.sceneCount}</td>
              <td>{user.quoteRequestCount}</td>
              <td>{new Date(user.createdAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  const renderScenesTab = () => (
    <div className="admin-table-container">
      <h2>Scene Management</h2>
      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>User</th>
            <th>Public</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {scenes.map(scene => (
            <tr key={scene.sceneId}>
              <td>{scene.sceneId}</td>
              <td>{scene.sceneName}</td>
              <td>{scene.userFullName} ({scene.userEmail})</td>
              <td>{scene.isPublic ? '✓' : '✗'}</td>
              <td>{new Date(scene.createdAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  const renderRequestsTab = () => (
    <div className="admin-requests-container">
      <div className="requests-list">
        <h2>Quote Requests</h2>
        <div className="requests-grid">
          {requests.map(request => (
            <div
              key={request.id}
              className={`request-card ${selectedRequest?.id === request.id ? 'selected' : ''}`}
              onClick={() => handleSelectRequest(request)}
            >
              <div className="request-header">
                <span className={`status-badge ${request.status.toLowerCase()}`}>
                  {request.status}
                </span>
                <span className="request-id">#{request.id}</span>
              </div>
              <h3>{request.userFullName}</h3>
              <p className="request-email">{request.userEmail}</p>
              <p className="request-date">{new Date(request.createdAt).toLocaleString()}</p>
            </div>
          ))}
        </div>
      </div>

      {selectedRequest && (
        <div className="request-details">
          <h2>Request Details</h2>
          
          <div className="detail-section">
            <h3>Customer Information</h3>
            <p><strong>Name:</strong> {selectedRequest.userFullName}</p>
            <p><strong>Email:</strong> {selectedRequest.userEmail}</p>
            {selectedRequest.userPhone && <p><strong>Phone:</strong> {selectedRequest.userPhone}</p>}
            {selectedRequest.userCompany && <p><strong>Company:</strong> {selectedRequest.userCompany}</p>}
          </div>

          <div className="detail-section">
            <h3>Request Details</h3>
            {selectedRequest.roomDimensions && (
              <p><strong>Room Dimensions:</strong> {selectedRequest.roomDimensions}</p>
            )}
            {selectedRequest.additionalNotes && (
              <div>
                <strong>Additional Notes:</strong>
                <p className="notes-text">{selectedRequest.additionalNotes}</p>
              </div>
            )}
          </div>

          {selectedRequest.sceneSnapshot && (
            <div className="detail-section">
              <h3>Scene Snapshot</h3>
              <img src={selectedRequest.sceneSnapshot} alt="Scene" className="scene-snapshot" />
            </div>
          )}

          <div className="detail-section">
            <h3>Update Status</h3>
            <select
              value={statusUpdate}
              onChange={(e) => setStatusUpdate(e.target.value)}
              className="admin-select"
            >
              <option value="PENDING">Pending</option>
              <option value="PROCESSING">Processing</option>
              <option value="QUOTED">Quoted</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>

          <div className="detail-section">
            <h3>Admin Response</h3>
            <textarea
              value={adminResponse}
              onChange={(e) => setAdminResponse(e.target.value)}
              className="admin-textarea"
              rows={5}
              placeholder="Enter your response to the customer..."
            />
          </div>

          <div className="detail-section">
            <h3>Upload Document</h3>
            <input
              type="file"
              onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
              className="file-input"
            />
            {uploadFile && (
              <button onClick={handleFileUpload} className="upload-btn" disabled={loading}>
                Upload File
              </button>
            )}
            {selectedRequest.documentUrl && (
              <p className="document-link">
                Current document: <a href={`${BACKEND_URL}${selectedRequest.documentUrl}`} target="_blank" rel="noopener noreferrer">View</a>
              </p>
            )}
          </div>

          <button onClick={handleUpdateRequest} className="update-btn" disabled={loading}>
            {loading ? 'Updating...' : 'Update Request'}
          </button>
        </div>
      )}
    </div>
  );

  const renderProductsTab = () => (
    <div className="admin-table-container">
      <h2>Product Management</h2>
      <p>Product CRUD operations will use the existing product endpoints at /api/products</p>
      <p>You can navigate to the product management interface or implement it here.</p>
    </div>
  );

  // Product CRUD handlers
  const handleCreateProduct = () => {
    setSelectedProduct(null);
    setProductForm({
      name: '',
      description: '',
      priceRange: 'MEDIUM',
      modelPath: '',
      thumbnail: '',
      mountingType: 'FLOOR',
      categoryId: categories.length > 0 ? categories[0].id : 0,
    });
    setShowProductModal(true);
  };

  const handleEditProduct = (product: Product) => {
    setSelectedProduct(product);
    setProductForm({
      name: product.name,
      description: product.description,
      priceRange: product.priceRange,
      modelPath: product.modelPath,
      thumbnail: product.thumbnail || '',
      mountingType: product.mountingType,
      categoryId: product.categoryId,
    });
    setShowProductModal(true);
  };

  const handleDeleteProduct = async (productId: number) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    setLoading(true);
    setError('');
    try {
      await ProductService.delete(productId);
      setProducts(products.filter(p => p.id !== productId));
      alert('Product deleted successfully!');
    } catch (err: any) {
      setError(err.message || 'Failed to delete product');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveProduct = async () => {
    if (!productForm.name || !productForm.categoryId) {
      alert('Please fill in all required fields');
      return;
    }

    setLoading(true);
    setError('');
    try {
      if (selectedProduct) {
        // Update existing product
        const updated = await ProductService.update(selectedProduct.id, {
          name: productForm.name,
          description: productForm.description,
          priceRange: productForm.priceRange,
          modelPath: productForm.modelPath,
          thumbnail: productForm.thumbnail,
          mountingType: productForm.mountingType,
          categoryId: productForm.categoryId,
        });
        setProducts(products.map(p => p.id === updated.id ? updated : p));
        alert('Product updated successfully!');
      } else {
        // Create new product
        const created = await ProductService.create({
          name: productForm.name,
          description: productForm.description,
          priceRange: productForm.priceRange,
          modelPath: productForm.modelPath,
          thumbnail: productForm.thumbnail,
          mountingType: productForm.mountingType,
          categoryId: productForm.categoryId,
        });
        setProducts([...products, created]);
        alert('Product created successfully!');
      }
      setShowProductModal(false);
    } catch (err: any) {
      setError(err.message || 'Failed to save product');
    } finally {
      setLoading(false);
    }
  };

  const renderProductModal = () => {
    if (!showProductModal) return null;

    return (
      <div className="modal-overlay" onClick={() => setShowProductModal(false)}>
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <h2>{selectedProduct ? 'Edit Product' : 'Create Product'}</h2>
          
          <div className="form-group">
            <label>Name *</label>
            <input
              type="text"
              value={productForm.name}
              onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
              placeholder="Product name"
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              value={productForm.description}
              onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
              placeholder="Product description"
              rows={3}
            />
          </div>

          <div className="form-group">
            <label>Category *</label>
            <select
              value={productForm.categoryId}
              onChange={(e) => setProductForm({ ...productForm, categoryId: Number(e.target.value) })}
            >
              <option value={0}>Select category</option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Price Range</label>
            <select
              value={productForm.priceRange}
              onChange={(e) => setProductForm({ ...productForm, priceRange: e.target.value as 'LOW' | 'MEDIUM' | 'HIGH' })}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="form-group">
            <label>Mounting Type</label>
            <select
              value={productForm.mountingType}
              onChange={(e) => setProductForm({ ...productForm, mountingType: e.target.value as 'FLOOR' | 'WALL' | 'FREESTANDING' })}
            >
              <option value="FLOOR">Floor</option>
              <option value="WALL">Wall</option>
              <option value="FREESTANDING">Freestanding</option>
            </select>
          </div>

          <div className="form-group">
            <label>Model Path</label>
            <input
              type="text"
              value={productForm.modelPath}
              onChange={(e) => setProductForm({ ...productForm, modelPath: e.target.value })}
              placeholder="/assets/models/product.glb"
            />
          </div>

          <div className="form-group">
            <label>Thumbnail URL</label>
            <input
              type="text"
              value={productForm.thumbnail}
              onChange={(e) => setProductForm({ ...productForm, thumbnail: e.target.value })}
              placeholder="/assets/thumbnails/product.png"
            />
          </div>

          <div className="modal-actions">
            <button onClick={() => setShowProductModal(false)} className="cancel-btn">
              Cancel
            </button>
            <button onClick={handleSaveProduct} className="save-btn" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </div>
      </div>
    );
  };

  const renderProductsTabContent = () => (
    <div className="admin-table-container">
      <div className="table-header">
        <h2>Product Management</h2>
        <button onClick={handleCreateProduct} className="create-btn">
          + Create Product
        </button>
      </div>
      
      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Price Range</th>
            <th>Mounting Type</th>
            <th>Model Path</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map(product => (
            <tr key={product.id}>
              <td>{product.id}</td>
              <td>{product.name}</td>
              <td>{product.categoryName}</td>
              <td>
                <span className={`price-badge ${product.priceRange.toLowerCase()}`}>
                  {product.priceRange}
                </span>
              </td>
              <td>{product.mountingType}</td>
              <td className="model-path">{product.modelPath}</td>
              <td>
                <button 
                  onClick={() => handleEditProduct(product)} 
                  className="edit-btn-small"
                  title="Edit"
                >
                  ✏️
                </button>
                <button 
                  onClick={() => handleDeleteProduct(product.id)} 
                  className="delete-btn-small"
                  title="Delete"
                >
                  🗑️
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {products.length === 0 && (
        <div className="empty-state">
          No products found. Click "Create Product" to add one.
        </div>
      )}
    </div>
  );

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
        <div className="admin-header-actions">
          <button className="btn-help" onClick={() => setIsHelpModalOpen(true)} title="Help">
            \u2753 Help
          </button>
          <button onClick={() => { authService.logout(); navigate('/'); }} className="logout-btn">
            Logout
          </button>
        </div>
      </div>

      <div className="admin-tabs">
        <button
          className={`tab-btn ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          Quote Requests
        </button>
        <button
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          Users
        </button>
        <button
          className={`tab-btn ${activeTab === 'scenes' ? 'active' : ''}`}
          onClick={() => setActiveTab('scenes')}
        >
          Scenes
        </button>
        <button
          className={`tab-btn ${activeTab === 'products' ? 'active' : ''}`}
          onClick={() => setActiveTab('products')}
        >
          Products
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="admin-content">
        {loading && <div className="loading">Loading...</div>}
        {!loading && activeTab === 'users' && renderUsersTab()}
        {!loading && activeTab === 'scenes' && renderScenesTab()}
        {!loading && activeTab === 'requests' && renderRequestsTab()}
        {!loading && activeTab === 'products' && renderProductsTabContent()}
      </div>

      {renderProductModal()}

      <HelpModal
        isOpen={isHelpModalOpen}
        onClose={() => setIsHelpModalOpen(false)}
        currentPage="admin"
      />
    </div>
  );
};

export default AdminDashboard;
